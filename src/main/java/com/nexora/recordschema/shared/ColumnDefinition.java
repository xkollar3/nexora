package com.nexora.recordschema.shared;

public record ColumnDefinition(
    String name,
    ColumnType type,
    boolean required
) {
}
