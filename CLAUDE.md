# Nexora

Agentic data management platform. Spring Boot Modulith with event-driven architecture and MongoDB.

## Tech Stack

- Java 25, Spring Boot 3.5.x, Spring Modulith 1.4.x
- MongoDB (via Spring Data MongoDB)
- Lombok for boilerplate reduction
- Testcontainers for E2E tests

## Hard Rules

- **Always use `UUID` for all identifiers** — entity IDs, command IDs, event IDs. Never `String`.
- **Never auto-generate IDs via defaults** — callers provide IDs explicitly (no `UUID.randomUUID()` in interfaces/defaults).
- **Use Lombok** — `@Getter`, `@NoArgsConstructor`, `@RequiredArgsConstructor` on classes. Use Java `record` for commands, events, DTOs.
- **Business logic lives in the aggregate** — aggregates accept commands and return events. Handlers are thin glue.

## Architecture: Vertical Slices within Spring Modulith

Each module lives under `com.nexora.<module>`. Inside a module:

- `shared/` — the aggregate root, value objects, types, and repository used across slices
- `<operationname>/` — one package per vertical slice (e.g. `createrecordschema/`)

### Aggregate Pattern

The aggregate is the single source of business logic. It has `handle()` methods that accept commands and return events:

```java
public RecordSchemaCreatedEvent handle(CreateRecordSchemaCommand command) { ... }
public AgentAssignedEvent handle(AssignAgentCommand command) { ... }
public AgentCustomizedEvent handle(CustomizeAgentCommand command) { ... }
```

The aggregate never persists itself or publishes events — that's the handler's job.

### Handler Pattern (Thin Glue)

Handlers are just wiring. They:
1. Retrieve or create the aggregate
2. Pass the command to the aggregate
3. Save the aggregate state
4. Publish the returned event

```java
var schema = repository.findById(command.schemaId()).orElseThrow(...);
var event = schema.handle(command);
repository.save(schema);
eventPublisher.publish(event);
```

### Vertical Slice Types

**REST-driven slice** (e.g. `createrecordschema/`, `customizeagent/`):
| File | Role |
|---|---|
| `<Op>Command.java` | Command record implementing `Command` |
| `<Op>Handler.java` | `@Component` implementing `CommandHandler<C, R>`, thin glue |
| `<Op>Controller.java` | `@RestController`, creates command, dispatches via `CommandDispatcher` |
| `<Op>Event.java` | Event record implementing `DomainEvent` |
| `<Op>E2ETest.java` | Extends `BaseE2ETest`, tests via MockMvc |

**Event-driven slice / Policy** (e.g. `assignagent/`):
| File | Role |
|---|---|
| `<Op>Command.java` | Command record implementing `Command` |
| `<Op>Policy.java` | `@Component` with `@EventListener`, creates command, routes to aggregate |
| `<Op>Event.java` | Event record implementing `DomainEvent` |
| Supporting classes | e.g. `SystemPromptGenerator` for domain logic outside the aggregate |
| `<Op>E2ETest.java` | Extends `BaseE2ETest`, tests indirectly via triggering action |

### Project Structure

```
com.nexora.recordschema/
  shared/
    RecordSchema.java              # aggregate with handle() methods
    RecordSchemaRepository.java    # public, shared across slices
    ColumnDefinition.java          # record value object
    ColumnType.java                # enum
    AgentOperations.java           # record with static factory
  createrecordschema/              # REST-driven: POST /api/record-schemas
    CreateRecordSchemaCommand.java
    CreateRecordSchemaHandler.java
    CreateRecordSchemaController.java
    RecordSchemaCreatedEvent.java
  assignagent/                     # Event-driven: reacts to RecordSchemaCreatedEvent
    AssignAgentCommand.java
    AssignAgentPolicy.java
    AgentAssignedEvent.java
    SystemPromptGenerator.java
  customizeagent/                  # REST-driven: PUT /api/record-schemas/{name}/agent
    CustomizeAgentCommand.java
    CustomizeAgentHandler.java
    CustomizeAgentController.java
    AgentCustomizedEvent.java
```

### Flow

1. **Controller/Policy** receives trigger (HTTP request or domain event), creates command, dispatches to handler
2. **Handler** retrieves/creates aggregate, passes command to `aggregate.handle(command)`, saves state, publishes returned event
3. **Event listeners** (`@EventListener` policies) in other slices react to published events

### Query Module (Read Side)

Queries live in a `query/` package within the aggregate module — **outside** vertical slices. This separates reads from command-driven writes.

```
com.nexora.recordschema/
  query/
    ListRecordsQuery.java          # implements Query
    ListRecordsQueryHandler.java   # implements QueryHandler, uses MongoTemplate
```

Controllers dispatch queries via `QueryDispatcher`, same pattern as commands:
```java
var query = new ListRecordsQuery(schema.getId(), page, size, filter);
return queryDispatcher.dispatch(query);
```

## Shared Infrastructure (`com.nexora.shared`)

### Commands (Write Side)
- `Command` — interface: `UUID commandId()`, `Instant timestamp()`
- `CommandHandler<C, R>` — interface: `R handle(C command)`, `Class<C> commandType()`
- `CommandDispatcher` — auto-discovers handlers, dispatches by command class

### Queries (Read Side)
- `Query` — marker interface
- `QueryHandler<Q, R>` — interface: `R handle(Q query)`, `Class<Q> queryType()`
- `QueryDispatcher` — auto-discovers handlers, dispatches by query class

### Events
- `DomainEvent` — interface: `UUID eventId()`, `Instant occurredAt()`
- `EventPublisher` — wraps Spring `ApplicationEventPublisher`

## Testing

- **E2E tests only** — no unit tests, test through the REST API
- All E2E tests extend `BaseE2ETest` (`src/test/java/com/nexora/BaseE2ETest.java`)
- Uses `MongoDBAtlasLocalContainer` with manual lifecycle (`static {}` start, `@AfterAll` stop)
- MongoDB connection configured via `@DynamicPropertySource`
- Use `MockMvc` to call endpoints, assert with `status()` and `jsonPath()`
- Use `MongoTemplate` to verify aggregate state in DB when needed

## Adding a New Vertical Slice

1. Create package `com.nexora.<module>.<operationname>`
2. Create command record implementing `Command`
3. Create event record implementing `DomainEvent`
4. Add `handle(<Command>)` method to the aggregate returning the event
5. Create handler (thin glue): load aggregate -> pass command -> save -> publish event
6. Create controller (REST) or policy (`@EventListener`) as the entry point
7. Write E2E test extending `BaseE2ETest`
8. Run `mvn test` to verify
