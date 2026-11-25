package com.umc.product.inquiry.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.inquiry.domain.enums.InquiryHistoryType;
import com.umc.product.inquiry.domain.enums.InquiryParticipant;
import com.umc.product.inquiry.domain.enums.InquiryStatus;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

class InquiryTest {

    private static final Instant CREATED_AT = Instant.parse("2026-11-24T00:00:00Z");
    private static final Long STAFF = 101L;
    private static final Long OTHER_STAFF = 102L;
    private static final Set<Long> ROSTER = Set.of(STAFF, OTHER_STAFF);

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

    @Test
    @DisplayName("다른 운영진이 담당자를 지정하면 이력이 남고 새 담당자에게 알린다")
    void 다른_운영진이_지정() {
        // given
        Inquiry inquiry = 저장된_문의();

        // when
        Optional<InquiryHistory> history = inquiry.changeAssignee(STAFF, OTHER_STAFF, null, ROSTER);

        // then
        assertThat(inquiry.getAssigneeMemberId()).isEqualTo(OTHER_STAFF);
        assertThat(history).hasValueSatisfying(saved -> {
            assertThat(saved.getType()).isEqualTo(InquiryHistoryType.ASSIGNED);
            assertThat(saved.getNewAssigneeMemberId()).isEqualTo(OTHER_STAFF);
            assertThat(saved.notifiesNewAssignee()).isTrue();
        });
    }

    @Test
    @DisplayName("스스로 맡거나 담당을 해제하면 이력은 남지만 아무에게도 알리지 않는다")
    void 스스로_맡기와_해제() {
        Inquiry inquiry = 저장된_문의();

        InquiryHistory assigned = inquiry.changeAssignee(STAFF, STAFF, null, ROSTER).orElseThrow();
        InquiryHistory unassigned = inquiry.changeAssignee(STAFF, null, STAFF, ROSTER).orElseThrow();

        assertThat(assigned.notifiesNewAssignee()).isFalse();
        assertThat(unassigned.getType()).isEqualTo(InquiryHistoryType.UNASSIGNED);
        assertThat(unassigned.notifiesNewAssignee()).isFalse();
    }

    @Test
    @DisplayName("요청자가 본 담당자와 지금 담당자가 다르면 바꾸지 않고 거절한다")
    void 본_담당자와_다름() {
        Inquiry inquiry = 저장된_문의();
        inquiry.changeAssignee(STAFF, STAFF, null, ROSTER);

        assertThatThrownBy(() -> inquiry.changeAssignee(OTHER_STAFF, OTHER_STAFF, null, ROSTER))
            .extracting("baseCode")
            .isEqualTo(InquiryErrorCode.INQUIRY_ASSIGNEE_CHANGED);
        assertThat(inquiry.getAssigneeMemberId()).isEqualTo(STAFF);
    }

    @Test
    @DisplayName("담당자 명단에 없는 회원은 담당자로 지정할 수 없다")
    void 담당_불가() {
        Inquiry inquiry = 저장된_문의();

        assertThatThrownBy(() -> inquiry.changeAssignee(STAFF, 999L, null, ROSTER))
            .extracting("baseCode")
            .isEqualTo(InquiryErrorCode.INQUIRY_ASSIGNEE_NOT_STAFF);
        assertThat(inquiry.getAssigneeMemberId()).isNull();
    }

    @Test
    @DisplayName("담당자가 명단에서 빠지면 미배정으로 보고, 본 담당자를 비운 채로 다시 지정할 수 있다")
    void 권한_잃은_담당자() {
        // given
        Inquiry inquiry = 저장된_문의();
        inquiry.changeAssignee(STAFF, STAFF, null, ROSTER);
        Set<Long> withoutStaff = Set.of(OTHER_STAFF);

        // when
        InquiryHistory history = inquiry.changeAssignee(OTHER_STAFF, OTHER_STAFF, null, withoutStaff).orElseThrow();

        // then
        assertThat(inquiry.effectiveAssigneeId(withoutStaff)).isEqualTo(OTHER_STAFF);
        assertThat(history.getType()).isEqualTo(InquiryHistoryType.ASSIGNED);
        assertThat(history.getPreviousAssigneeMemberId()).isNull();
    }

    private Inquiry 저장된_문의() {
        Inquiry inquiry = 문의();
        ReflectionTestUtils.setField(inquiry, "id", 1L);
        return inquiry;
    }

    private Inquiry 문의() {
        return 문의("앱 로그인이 안 돼요");
    }

    private Inquiry 문의(String content) {
        return Inquiry.create(1L, 1L, null, content, 10L, UUID.randomUUID(), null, CREATED_AT);
    }
}
