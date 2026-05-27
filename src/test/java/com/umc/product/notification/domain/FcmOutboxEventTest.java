package com.umc.product.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FcmOutboxEventTest {

    @Test
    @DisplayName("create는 매번 새로운 인스턴스와 새로운 eventId를 반환한다")
    void create는_매번_새_인스턴스_반환() {
        // when
        FcmOutboxEvent first = FcmOutboxEvent.create();
        FcmOutboxEvent second = FcmOutboxEvent.create();

        // then
        assertThat(first.eventId()).isNotEqualTo(second.eventId());
    }

    @Test
    @DisplayName("create로 생성된 이벤트의 occurredAt은 호출 시점 근처여야 한다")
    void create는_호출_시점에_occurredAt_설정() {
        // given
        Instant before = Instant.now();

        // when
        FcmOutboxEvent event = FcmOutboxEvent.create();

        // then
        Instant after = Instant.now();
        assertThat(event.occurredAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("eventType은 'fcm.outbox.created'이다")
    void eventType은_고정값() {
        // when
        FcmOutboxEvent event = FcmOutboxEvent.create();

        // then
        assertThat(event.eventType()).isEqualTo("fcm.outbox.created");
    }
}
