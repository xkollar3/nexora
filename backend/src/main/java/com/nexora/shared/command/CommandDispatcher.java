package com.nexora.shared.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dispatches commands to their appropriate handlers.
 * Automatically discovers and registers all CommandHandler beans.
 */
@Component
public class CommandDispatcher {

    private static final Logger log = LoggerFactory.getLogger(CommandDispatcher.class);

    private final Map<Class<? extends Command>, CommandHandler<?, ?>> handlers = new HashMap<>();

    public CommandDispatcher(List<CommandHandler<?, ?>> commandHandlers) {
        for (CommandHandler<?, ?> handler : commandHandlers) {
            handlers.put(handler.commandType(), handler);
            log.info("Registered command handler: {} for command: {}",
                handler.getClass().getSimpleName(),
                handler.commandType().getSimpleName());
        }
    }

    @SuppressWarnings("unchecked")
    public <C extends Command, R> R dispatch(C command) {
        CommandHandler<C, R> handler = (CommandHandler<C, R>) handlers.get(command.getClass());

        if (handler == null) {
            throw new IllegalArgumentException(
                "No handler registered for command: " + command.getClass().getSimpleName());
        }

        log.debug("Dispatching command: {} with id: {}",
            command.getClass().getSimpleName(), command.commandId());

        return handler.handle(command);
    }
}
