package com.nexora.recordschema.query;

import com.nexora.recordschema.shared.Record;
import com.nexora.recordschema.shared.RecordSchemaRepository;
import com.nexora.shared.query.QueryDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
class RecordQueryController {

    private final QueryDispatcher queryDispatcher;
    private final RecordSchemaRepository schemaRepository;

    @GetMapping("/{schemaName}")
    Page<Record> list(
        @PathVariable String schemaName,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "{}") String filter
    ) {
        schemaRepository.findByName(schemaName)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Schema not found: " + schemaName));

        var query = new ListRecordsQuery(schemaName, page, size, filter);

        return queryDispatcher.dispatch(query);
    }
}
