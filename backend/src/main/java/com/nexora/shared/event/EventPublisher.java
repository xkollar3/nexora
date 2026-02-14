package com.nexora.shared.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publishes domain events to Spring's event system.
 * Events can be picked up by @EventListener or @TransactionalEventListener.
 */
@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public EventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publish(DomainEvent event) {
        log.debug("Publishing event: {} with id: {}",
            event.getClass().getSimpleName(), event.eventId());

        applicationEventPublisher.publishEvent(event);
    }
}
