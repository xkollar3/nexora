package com.nexora.recordschema.query;

import com.nexora.recordschema.shared.RecordSchema;
import com.nexora.shared.query.QueryDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/record-schemas")
@RequiredArgsConstructor
class RecordSchemaQueryController {

    private final QueryDispatcher queryDispatcher;

    @GetMapping
    List<RecordSchema> list() {
        return queryDispatcher.dispatch(new ListSchemasQuery());
    }
}
