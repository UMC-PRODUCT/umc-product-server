package com.umc.product.global.event.application.port.out;

import com.umc.product.global.event.domain.EventOutbox;
import java.util.Collection;

public interface SaveEventOutboxPort {

    void save(EventOutbox eventOutbox);

    void saveAll(Collection<EventOutbox> eventOutboxes);
}
