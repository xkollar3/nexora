package com.nexora.recordschema.deleterecord;

import com.nexora.BaseE2ETest;
import com.nexora.recordschema.shared.Record;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeleteRecordE2ETest extends BaseE2ETest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    void shouldDeleteExistingRecord() throws Exception {
        createSchema("Items");
        var recordId = insertRecord("Items", """
            { "name": "ToDelete" }
            """);

        mockMvc.perform(delete("/api/records/Items/" + recordId))
            .andExpect(status().isNoContent());

        var records = mongoTemplate.find(new Query(), Record.class, "Items");
        assertThat(records).noneMatch(r ->
            "ToDelete".equals(r.getFields().get("name"))
        );
    }

    @Test
    void shouldReturn404ForNonExistentRecord() throws Exception {
        createSchema("Widgets");

        mockMvc.perform(delete("/api/records/Widgets/00000000-0000-0000-0000-000000000000"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404ForNonExistentSchema() throws Exception {
        mockMvc.perform(delete("/api/records/NoSuchSchema/00000000-0000-0000-0000-000000000000"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAffectOtherRecords() throws Exception {
        createSchema("Notes");
        insertRecord("Notes", """
            { "name": "Keep" }
            """);
        var toDelete = insertRecord("Notes", """
            { "name": "Remove" }
            """);

        mockMvc.perform(delete("/api/records/Notes/" + toDelete))
            .andExpect(status().isNoContent());

        var records = mongoTemplate.find(new Query(), Record.class, "Notes");
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getFields().get("name")).isEqualTo("Keep");
    }

    private void createSchema(String name) throws Exception {
        mockMvc.perform(post("/api/record-schemas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "%s",
                        "columns": [
                            { "name": "name", "type": "TEXT", "required": true }
                        ]
                    }
                    """.formatted(name)))
            .andExpect(status().isCreated());
    }

    private String insertRecord(String schemaName, String fields) throws Exception {
        var result = mockMvc.perform(post("/api/records/" + schemaName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(fields))
            .andExpect(status().isCreated())
            .andReturn();

        return com.jayway.jsonpath.JsonPath.read(
            result.getResponse().getContentAsString(), "$.id");
    }
}
