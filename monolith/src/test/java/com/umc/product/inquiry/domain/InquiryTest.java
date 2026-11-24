package com.umc.product.inquiry.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.inquiry.domain.enums.InquiryParticipant;
import com.umc.product.inquiry.domain.enums.InquiryStatus;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

class InquiryTest {

    private static final Instant CREATED_AT = Instant.parse("2026-11-24T00:00:00Z");

    @Test
    @DisplayName("종료된 문의에 메시지가 오면 다시 진행 중이 되고 종료 시각이 지워진다")
    void 종료된_문의_재오픈() {
        // given
        Inquiry inquiry = 문의();
        inquiry.close(CREATED_AT.plusSeconds(10));

        // when
        inquiry.recordMessage(InquiryParticipant.AUTHOR, CREATED_AT.plusSeconds(20));

        // then
        assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.IN_PROGRESS);
        assertThat(inquiry.getClosedAt()).isNull();
        assertThat(inquiry.getLastMessageAt()).isEqualTo(CREATED_AT.plusSeconds(20));
    }

    @Test
    @DisplayName("이미 종료된 문의를 다시 종료하면 예외가 발생한다")
    void 이미_종료된_문의_종료() {
        // given
        Inquiry inquiry = 문의();
        inquiry.close(CREATED_AT.plusSeconds(10));

        // when & then
        assertThatThrownBy(() -> inquiry.close(CREATED_AT.plusSeconds(20)))
            .isInstanceOf(InquiryDomainException.class)
            .extracting("baseCode")
            .isEqualTo(InquiryErrorCode.INQUIRY_ALREADY_CLOSED);
    }

    @Test
    @DisplayName("제목이 비었거나 100자를 넘으면 문의를 만들 수 없다")
    void 제목_검증() {
        assertThatThrownBy(() -> 문의("   "))
            .extracting("baseCode")
            .isEqualTo(InquiryErrorCode.INVALID_INQUIRY_TITLE);
        assertThatThrownBy(() -> 문의("가".repeat(101)))
            .extracting("baseCode")
            .isEqualTo(InquiryErrorCode.INVALID_INQUIRY_TITLE);
    }

    private Inquiry 문의() {
        return 문의("앱 로그인이 안 돼요");
    }

    private Inquiry 문의(String title) {
        return Inquiry.create(1L, 1L, null, title, 10L, UUID.randomUUID(), CREATED_AT);
    }
}
