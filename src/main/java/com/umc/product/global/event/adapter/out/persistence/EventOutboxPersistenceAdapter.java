package com.umc.product.global.event.adapter.out.persistence;

import com.umc.product.global.event.application.port.out.LoadEventOutboxPort;
import com.umc.product.global.event.application.port.out.SaveEventOutboxPort;
import com.umc.product.global.event.domain.EventOutbox;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventOutboxPersistenceAdapter implements SaveEventOutboxPort, LoadEventOutboxPort {

    private final EventOutboxJpaRepository eventOutboxJpaRepository;

    @Override
    public void save(EventOutbox eventOutbox) {
        eventOutboxJpaRepository.save(eventOutbox);
    }

    @Override
    public List<EventOutbox> listPublishable(int limit, Instant now) {
        return eventOutboxJpaRepository.findPublishableForUpdate(limit, now);
    }
}
