package com.umc.product.global.event.adapter.out;

import com.umc.product.global.event.application.port.out.DomainEventPublisher;
import com.umc.product.global.event.application.port.out.SaveEventOutboxPort;
import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.global.event.domain.EventOutbox;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.event-outbox.enabled", havingValue = "true")
public class OutboxDomainEventPublisher implements DomainEventPublisher {

    private final SaveEventOutboxPort saveEventOutboxPort;
    private final EventPayloadSerializer serializer;

    @Override
    public void publish(DomainEvent event) {
        saveEventOutboxPort.save(EventOutbox.record(event, serializer.serialize(event)));
    }

    @Override
    public void publishAll(Collection<? extends DomainEvent> events) {
        events.forEach(this::publish);
    }
}
