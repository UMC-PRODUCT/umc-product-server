package com.umc.product.global.event.adapter.out.persistence;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.umc.product.global.event.application.port.out.LoadEventOutboxPort;
import com.umc.product.global.event.application.port.out.OutboxEventSelection;
import com.umc.product.global.event.application.port.out.SaveEventOutboxPort;
import com.umc.product.global.event.domain.EventOutbox;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventOutboxPersistenceAdapter implements SaveEventOutboxPort, LoadEventOutboxPort {

    private final EventOutboxJpaRepository eventOutboxJpaRepository;

    private final EventOutboxQueryRepository eventOutboxQueryRepository;

    @Override
    public List<EventOutbox> listPublishable(OutboxEventSelection selection, int limit, Instant now) {
        return eventOutboxQueryRepository.findPublishableForUpdate(selection, limit, now);
    }

    @Override
    public void save(EventOutbox eventOutbox) {
        eventOutboxJpaRepository.save(eventOutbox);
    }

    @Override
    public void saveAll(Collection<EventOutbox> eventOutboxes) {
        eventOutboxJpaRepository.saveAll(eventOutboxes);
    }

}
