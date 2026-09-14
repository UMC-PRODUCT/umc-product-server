package com.umc.product.audit.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.global.exception.constant.Domain;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuditLogEventTest {

    @Test
    @DisplayName("eventId와 occurredAt을 지정하지 않으면 기본값이 자동 주입된다")
    void 메타데이터_미지정시_기본값_자동주입() {
        // given
        Instant before = Instant.now();

        // when
        AuditLogEvent event = AuditLogEvent.builder()
            .domain(Domain.MEMBER)
            .action(AuditAction.REGISTER)
            .targetType("Member")
            .targetId("1")
            .build();

        // then
        Instant after = Instant.now();
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("eventId와 occurredAt을 명시하면 그 값이 그대로 유지된다")
    void 메타데이터_명시시_값_유지() {
        // given
        UUID givenId = UUID.randomUUID();
        Instant givenInstant = Instant.parse("2026-01-01T00:00:00Z");

        // when
        AuditLogEvent event = AuditLogEvent.builder()
            .eventId(givenId)
            .occurredAt(givenInstant)
            .domain(Domain.MEMBER)
            .action(AuditAction.REGISTER)
            .targetType("Member")
            .targetId("1")
            .build();

        // then
        assertThat(event.eventId()).isEqualTo(givenId);
        assertThat(event.occurredAt()).isEqualTo(givenInstant);
    }

    @Test
    @DisplayName("eventType은 'audit.log.<action>' 형식으로 생성된다")
    void eventType은_action_기반으로_생성() {
        // given
        AuditLogEvent registerEvent = AuditLogEvent.builder()
            .domain(Domain.MEMBER)
            .action(AuditAction.REGISTER)
            .targetType("Member")
            .targetId("1")
            .build();
        AuditLogEvent withdrawEvent = AuditLogEvent.builder()
            .domain(Domain.MEMBER)
            .action(AuditAction.WITHDRAW)
            .targetType("Member")
            .targetId("1")
            .build();

        // then
        assertThat(registerEvent.eventType()).isEqualTo("audit.log.register");
        assertThat(withdrawEvent.eventType()).isEqualTo("audit.log.withdraw");
    }
}
