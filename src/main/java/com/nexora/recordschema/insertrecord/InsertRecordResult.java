package com.nexora.recordschema.insertrecord;

import com.nexora.recordschema.shared.Record;

public record InsertRecordResult(
    Record record,
    RecordInsertedEvent event
) {
}
