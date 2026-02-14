package com.nexora.recordschema.insertrecord;

import com.nexora.BaseE2ETest;
import com.nexora.recordschema.shared.Record;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InsertRecordE2ETest extends BaseE2ETest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    void shouldInsertValidRecord() throws Exception {
        createSchema("Products");

        mockMvc.perform(post("/api/records/Products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Widget",
                        "price": 9.99,
                        "inStock": true
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty());

        var records = mongoTemplate.find(new Query(), Record.class, "Products");
        assertThat(records).anyMatch(r ->
            "Widget".equals(r.getFields().get("title"))
        );
    }

    @Test
    void shouldRejectRecordMissingRequiredColumn() throws Exception {
        createSchema("Orders");

        mockMvc.perform(post("/api/records/Orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "customer": "Acme Corp"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRecordWithUnknownColumn() throws Exception {
        createSchema("Tasks");

        mockMvc.perform(post("/api/records/Tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Do stuff",
                        "bogusField": "should not be here"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRecordForNonExistentSchema() throws Exception {
        mockMvc.perform(post("/api/records/DoesNotExist")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "foo": "bar"
                    }
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
                            { "name": "title", "type": "TEXT", "required": true },
                            { "name": "price", "type": "NUMBER", "required": false },
                            { "name": "inStock", "type": "BOOLEAN", "required": false },
                            { "name": "customer", "type": "TEXT", "required": false }
                        ]
                    }
                    """.formatted(name)))
            .andExpect(status().isCreated());
    }
}
