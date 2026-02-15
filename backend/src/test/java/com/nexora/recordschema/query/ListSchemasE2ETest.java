package com.nexora.recordschema.query;

import com.nexora.BaseE2ETest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ListSchemasE2ETest extends BaseE2ETest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void shouldReturnCreatedSchemas() throws Exception {
        createSchema("Products", """
            [
                { "name": "title", "type": "TEXT", "required": true },
                { "name": "price", "type": "NUMBER", "required": true }
            ]
            """);

        mockMvc.perform(get("/api/record-schemas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name == 'Products')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'Products')].columns.length()").value(2))
            .andExpect(jsonPath("$[?(@.name == 'Products')].columns[0].name").value("title"));
    }

    @Test
    void shouldReturnSchemaWithAgentOperations() throws Exception {
        createSchema("Invoices", """
            [{ "name": "amount", "type": "NUMBER", "required": true }]
            """, """
            { "createEnabled": true, "updateEnabled": false, "deleteEnabled": false }
            """);

        mockMvc.perform(get("/api/record-schemas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name == 'Invoices')].agentOperations.createEnabled").value(true))
            .andExpect(jsonPath("$[?(@.name == 'Invoices')].agentOperations.updateEnabled").value(false))
            .andExpect(jsonPath("$[?(@.name == 'Invoices')].agentOperations.deleteEnabled").value(false));
    }

    private void createSchema(String name, String columns) throws Exception {
        createSchema(name, columns, null);
    }

    private void createSchema(String name, String columns, String agentOperations) throws Exception {
        var agentOpsJson = agentOperations != null
            ? ", \"agentOperations\": %s".formatted(agentOperations)
            : "";

        mockMvc.perform(post("/api/record-schemas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "%s",
                        "columns": %s
                        %s
                    }
                    """.formatted(name, columns, agentOpsJson)))
            .andExpect(status().isCreated());
    }
}
