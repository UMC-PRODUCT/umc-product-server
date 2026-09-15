package com.umc.product.global.event.application.port.out;

import java.time.Instant;
import java.util.List;

import com.umc.product.global.event.domain.EventOutbox;

public interface LoadEventOutboxPort {

    List<EventOutbox> listPublishable(int limit, Instant now);
}
