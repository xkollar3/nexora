package com.nexora.recordschema.assignagent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.nexora.BaseE2ETest;
import com.nexora.recordschema.shared.RecordSchema;

class AssignAgentPolicyE2ETest extends BaseE2ETest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  MongoTemplate mongoTemplate;

  @Test
  void shouldAssignAgentWithSystemPromptAfterSchemaCreation() throws Exception {
    mockMvc.perform(post("/api/record-schemas")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
                "name": "Invoices",
                "columns": [
                    { "name": "vendor", "type": "TEXT", "required": true },
                    { "name": "amount", "type": "NUMBER", "required": true },
                    { "name": "paid", "type": "BOOLEAN", "required": false }
                ],
                "agentOperations": {
                    "createEnabled": true,
                    "updateEnabled": true,
                    "deleteEnabled": false
                }
            }
            """))
        .andExpect(status().isCreated());

    var schemas = mongoTemplate.findAll(RecordSchema.class);
    var invoiceSchema = schemas.stream()
        .filter(s -> "Invoices".equals(s.getName()))
        .findFirst()
        .orElseThrow();

    assertThat(invoiceSchema.getAgentSystemPrompt()).isNotNull();
    assertThat(invoiceSchema.getAgentSystemPrompt()).contains("Invoices");
    assertThat(invoiceSchema.getAgentSystemPrompt()).contains("vendor");
    assertThat(invoiceSchema.getAgentSystemPrompt()).contains("Create new records");
    assertThat(invoiceSchema.getAgentSystemPrompt()).contains("Update existing records");
    assertThat(invoiceSchema.getAgentSystemPrompt()).doesNotContain("Delete records");
  }
}
