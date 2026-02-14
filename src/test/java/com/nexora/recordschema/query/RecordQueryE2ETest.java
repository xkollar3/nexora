package com.nexora.recordschema.query;

import com.nexora.BaseE2ETest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RecordQueryE2ETest extends BaseE2ETest {

  @Autowired
  MockMvc mockMvc;

  @Test
  void shouldReturn404ForNonExistentSchema() throws Exception {
    mockMvc.perform(get("/api/records/NonExistent"))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturnInsertedRecords() throws Exception {
    createSchema("Contacts", """
        [
            { "name": "fullName", "type": "TEXT", "required": true },
            { "name": "age", "type": "NUMBER", "required": true }
        ]
        """);

    insertRecord("Contacts", """
        { "fullName": "Alice", "age": 30 }
        """);
    insertRecord("Contacts", """
        { "fullName": "Bob", "age": 25 }
        """);

    mockMvc.perform(get("/api/records/Contacts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.content.length()").value(2));
  }

  @Test
    void shouldNotReturnRecordsFromOtherSchemas() throws Exception {
        createSchema("Cats", """
            [{ "name": "breed", "type": "TEXT", "required": true }]
            """);
        createSchema("Dogs", """
            [{ "name": "breed", "type": "TEXT", "required": true }]
            """);

        insertRecord("Cats", """
            { "breed": "Siamese" }
            """);
        insertRecord("Dogs", """
            { "breed": "Labrador" }
            """);
        insertRecord("Dogs", """
            { "breed": "Poodle" }
            """);

        mockMvc.perform(get("/api/records/Cats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content.length()").value(1));
    }

  @Test
    void shouldFilterRecordsWithMongoQuery() throws Exception {
        createSchema("Metrics", """
            [
                { "name": "metric", "type": "TEXT", "required": true },
                { "name": "value", "type": "NUMBER", "required": true }
            ]
            """);

        insertRecord("Metrics", """
            { "metric": "cpu", "value": 85 }
            """);
        insertRecord("Metrics", """
            { "metric": "memory", "value": 60 }
            """);
        insertRecord("Metrics", """
            { "metric": "disk", "value": 92 }
            """);

        mockMvc.perform(get("/api/records/Metrics")
                .param("filter", "{\"fields.value\": {\"$gte\": 80}}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.content.length()").value(2));
    }

  @Test
  void shouldPaginateResults() throws Exception {
    createSchema("Logs", """
        [{ "name": "message", "type": "TEXT", "required": true }]
        """);

    for (int i = 1; i <= 5; i++) {
      insertRecord("Logs", """
          { "message": "Log entry %d" }
          """.formatted(i));
    }

    mockMvc.perform(get("/api/records/Logs")
        .param("page", "0")
        .param("size", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.totalElements").value(5))
        .andExpect(jsonPath("$.totalPages").value(3));

    mockMvc.perform(get("/api/records/Logs")
        .param("page", "2")
        .param("size", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.totalElements").value(5));
  }

  @Test
  void shouldReturnEmptyPageForSchemaWithNoRecords() throws Exception {
    createSchema("Empty", """
        [{ "name": "placeholder", "type": "TEXT", "required": false }]
        """);

    mockMvc.perform(get("/api/records/Empty"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(0))
        .andExpect(jsonPath("$.content.length()").value(0));
  }

  private void createSchema(String name, String columns) throws Exception {
    mockMvc.perform(post("/api/record-schemas")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
                "name": "%s",
                "columns": %s
            }
            """.formatted(name, columns)))
        .andExpect(status().isCreated());
  }

  private void insertRecord(String schemaName, String fields) throws Exception {
    mockMvc.perform(post("/api/records/" + schemaName)
        .contentType(MediaType.APPLICATION_JSON)
        .content(fields))
        .andExpect(status().isCreated());
  }
}
