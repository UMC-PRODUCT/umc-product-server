package com.umc.product.global.event.application.port.out;

import com.umc.product.global.event.domain.EventOutbox;

public interface SaveEventOutboxPort {

    void save(EventOutbox eventOutbox);
}
