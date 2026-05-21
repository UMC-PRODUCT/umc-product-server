package com.umc.product.global.event.application.port.out;

import com.umc.product.global.event.domain.EventOutbox;
import java.time.Instant;
import java.util.List;

public interface LoadEventOutboxPort {

    List<EventOutbox> listPublishable(int limit, Instant now);
}
