package com.nexora.shared.websocket;

import com.nexora.recordschema.deleterecord.RecordDeletedEvent;
import com.nexora.recordschema.insertrecord.RecordInsertedEvent;
import com.nexora.recordschema.updaterecord.RecordUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordChangeBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void on(RecordInsertedEvent event) {
        broadcast(new RecordChangeEvent("INSERTED", event.recordId(), event.schemaId(), event.occurredAt()));
    }

    @EventListener
    public void on(RecordUpdatedEvent event) {
        broadcast(new RecordChangeEvent("UPDATED", event.recordId(), event.schemaId(), event.occurredAt()));
    }

    @EventListener
    public void on(RecordDeletedEvent event) {
        broadcast(new RecordChangeEvent("DELETED", event.recordId(), event.schemaId(), event.occurredAt()));
    }

    private void broadcast(RecordChangeEvent change) {
        var destination = "/topic/schemas/" + change.schemaId();
        log.debug("Broadcasting {} to {}", change.type(), destination);
        messagingTemplate.convertAndSend(destination, change);
    }
}
