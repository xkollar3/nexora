package com.nexora.recordschema.createrecordschema;

import com.nexora.BaseE2ETest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CreateRecordSchemaE2ETest extends BaseE2ETest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void shouldCreateRecordSchemaWithAllOperationsEnabled() throws Exception {
        mockMvc.perform(post("/api/record-schemas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Contracts",
                        "columns": [
                            { "name": "title", "type": "TEXT", "required": true },
                            { "name": "amount", "type": "NUMBER", "required": true },
                            { "name": "signed", "type": "BOOLEAN", "required": false }
                        ]
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void shouldCreateRecordSchemaWithCustomAgentOperations() throws Exception {
        mockMvc.perform(post("/api/record-schemas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "ReadOnlyLedger",
                        "columns": [
                            { "name": "entry", "type": "TEXT", "required": true }
                        ],
                        "agentOperations": {
                            "createEnabled": true,
                            "updateEnabled": false,
                            "deleteEnabled": false
                        }
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty());
    }
}
