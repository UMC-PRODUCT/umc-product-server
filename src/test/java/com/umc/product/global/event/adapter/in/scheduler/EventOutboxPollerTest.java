package com.umc.product.global.event.adapter.in.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.umc.product.global.event.application.service.EventOutboxRelayService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EventOutboxPoller")
class EventOutboxPollerTest {

    @Test
    @DisplayName("poll은 relay service를 호출한다")
    void poll() {
        EventOutboxRelayService relayService = mock(EventOutboxRelayService.class);
        EventOutboxPoller poller = new EventOutboxPoller(relayService);

        poller.poll();

        verify(relayService).relay();
    }
}
