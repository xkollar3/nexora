package com.nexora.recordschema.shared;

import java.util.List;

public class RecordValidationException extends RuntimeException {

    private final List<String> errors;

    public RecordValidationException(List<String> errors) {
        super("Record validation failed: " + String.join("; ", errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
