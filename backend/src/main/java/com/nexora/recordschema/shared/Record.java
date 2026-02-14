package com.nexora.recordschema.shared;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Map;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class Record {

    @Id
    private UUID id;
    private Map<String, Object> fields;

    public Record(UUID id, Map<String, Object> fields) {
        this.id = id;
        this.fields = fields;
    }
}
