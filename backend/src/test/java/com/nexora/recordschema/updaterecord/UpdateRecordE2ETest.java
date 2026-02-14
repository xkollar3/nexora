package com.nexora.recordschema.updaterecord;

import com.nexora.BaseE2ETest;
import com.nexora.recordschema.shared.Record;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UpdateRecordE2ETest extends BaseE2ETest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    void shouldUpdateExistingRecord() throws Exception {
        createSchema("People");
        var recordId = insertRecord("People", """
            { "name": "Alice", "age": 30 }
            """);

        mockMvc.perform(patch("/api/records/People/" + recordId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "age": 31 }
                    """))
            .andExpect(status().isOk());

        var record = findRecord("People", recordId);
        assertThat(record.getFields().get("age")).isEqualTo(31);
        assertThat(record.getFields().get("name")).isEqualTo("Alice");
    }

    @Test
    void shouldRejectUpdateWithUnknownField() throws Exception {
        createSchema("Cars");
        var recordId = insertRecord("Cars", """
            { "name": "Civic", "age": 5 }
            """);

        mockMvc.perform(patch("/api/records/Cars/" + recordId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "bogus": "field" }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUpdateWithWrongType() throws Exception {
        createSchema("Books");
        var recordId = insertRecord("Books", """
            { "name": "Java", "age": 1 }
            """);

        mockMvc.perform(patch("/api/records/Books/" + recordId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "age": "not a number" }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404ForNonExistentRecord() throws Exception {
        createSchema("Tags");

        mockMvc.perform(patch("/api/records/Tags/00000000-0000-0000-0000-000000000000")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "name": "updated" }
                    """))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404ForNonExistentSchema() throws Exception {
        mockMvc.perform(patch("/api/records/NoSchema/00000000-0000-0000-0000-000000000000")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "name": "updated" }
                    """))
            .andExpect(status().isNotFound());
    }

    private void createSchema(String name) throws Exception {
        mockMvc.perform(post("/api/record-schemas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "%s",
                        "columns": [
                            { "name": "name", "type": "TEXT", "required": true },
                            { "name": "age", "type": "NUMBER", "required": false }
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

    private Record findRecord(String collectionName, String recordId) {
        var query = new Query(Criteria.where("_id").is(UUID.fromString(recordId)));
        return mongoTemplate.findOne(query, Record.class, collectionName);
    }
}
