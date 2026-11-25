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
    @DisplayName("제목은 내용의 첫 번째 비지 않은 줄을 앞뒤 공백 없이 쓰고, 50자를 넘으면 자른다")
    void 내용으로_제목_만들기() {
        assertThat(문의("  출석 체크가 안 돼요  \n어제부터요").getTitle()).isEqualTo("출석 체크가 안 돼요");
        assertThat(문의("\n   \n  두 번째 줄이 제목\n세 번째").getTitle()).isEqualTo("두 번째 줄이 제목");
        assertThat(문의("가".repeat(60)).getTitle()).isEqualTo("가".repeat(50));
        assertThat(문의("😀".repeat(51)).getTitle()).isEqualTo("😀".repeat(50));
    }

    @Test
    @DisplayName("내용이 공백뿐이면 문의를 만들 수 없다")
    void 빈_내용() {
        assertThatThrownBy(() -> 문의(" \n  "))
            .extracting("baseCode")
            .isEqualTo(InquiryErrorCode.INQUIRY_CONTENT_REQUIRED);
    }

    private Inquiry 문의() {
        return 문의("앱 로그인이 안 돼요");
    }

    private Inquiry 문의(String content) {
        return Inquiry.create(1L, 1L, null, content, 10L, UUID.randomUUID(), CREATED_AT);
    }
}
