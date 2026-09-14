package com.umc.product.authentication.application.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SendVerificationEmailEventTest {

    @Test
    @DisplayName("of 정적 팩토리는 eventId와 occurredAt을 자동 주입한다")
    void of_메타데이터_자동주입() {
        // given
        Instant before = Instant.now();

        // when
        SendVerificationEmailEvent event = SendVerificationEmailEvent.of("user@example.com", "123456");

        // then
        Instant after = Instant.now();
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isBetween(before, after);
        assertThat(event.email()).isEqualTo("user@example.com");
        assertThat(event.verificationCode()).isEqualTo("123456");
    }

    @Test
    @DisplayName("of 정적 팩토리는 매번 새로운 eventId를 반환한다")
    void of는_매번_새_eventId_반환() {
        // when
        SendVerificationEmailEvent first = SendVerificationEmailEvent.of("a@example.com", "111111");
        SendVerificationEmailEvent second = SendVerificationEmailEvent.of("a@example.com", "111111");

        // then
        assertThat(first.eventId()).isNotEqualTo(second.eventId());
    }

    @Test
    @DisplayName("eventId와 occurredAt을 명시하면 그 값이 그대로 유지된다")
    void 메타데이터_명시시_값_유지() {
        // given
        UUID givenId = UUID.randomUUID();
        Instant givenInstant = Instant.parse("2026-01-01T00:00:00Z");

        // when
        SendVerificationEmailEvent event = new SendVerificationEmailEvent(
            givenId, givenInstant, "user@example.com", "654321"
        );

        // then
        assertThat(event.eventId()).isEqualTo(givenId);
        assertThat(event.occurredAt()).isEqualTo(givenInstant);
    }

    @Test
    @DisplayName("eventType은 'authentication.email.verification.requested'이다")
    void eventType은_고정값() {
        // when
        SendVerificationEmailEvent event = SendVerificationEmailEvent.of("user@example.com", "123456");

        // then
        assertThat(event.eventType()).isEqualTo("authentication.email.verification.requested");
    }
}
