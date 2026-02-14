package com.nexora.recordschema.customizeagent;

import com.nexora.BaseE2ETest;
import com.nexora.recordschema.shared.RecordSchema;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CustomizeAgentE2ETest extends BaseE2ETest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    void shouldCustomizeAgentWithAdditionalContext() throws Exception {
        mockMvc.perform(post("/api/record-schemas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Employees",
                        "columns": [
                            { "name": "fullName", "type": "TEXT", "required": true },
                            { "name": "startDate", "type": "DATE", "required": true },
                            { "name": "active", "type": "BOOLEAN", "required": false }
                        ]
                    }
                    """))
            .andExpect(status().isCreated());

        mockMvc.perform(put("/api/record-schemas/Employees/agent")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "additionalContext": "Employees must have a start date in the past. Inactive employees should not be modified."
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNotEmpty());

        var schema = mongoTemplate.findAll(RecordSchema.class).stream()
            .filter(s -> "Employees".equals(s.getName()))
            .findFirst()
            .orElseThrow();

        assertThat(schema.getAdditionalContext())
            .contains("start date in the past");
        assertThat(schema.getAdditionalContext())
            .contains("Inactive employees");
    }

    @Test
    void shouldRejectCustomizationForNonExistentSchema() throws Exception {
        mockMvc.perform(put("/api/record-schemas/NonExistent/agent")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "additionalContext": "Some context"
                    }
                    """))
            .andExpect(status().isNotFound());
    }
}
