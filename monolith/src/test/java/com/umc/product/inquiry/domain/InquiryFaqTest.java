package com.umc.product.inquiry.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.inquiry.domain.enums.InquiryFaqStatus;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

class InquiryFaqTest {

    private static final Long STAFF_ID = 1L;
    private static final Long OTHER_STAFF_ID = 2L;
    private static final Instant NOW = Instant.parse("2026-11-25T03:00:00Z");

    @Test
    @DisplayName("초안을 발행하면 수정본이 발행본이 되고 발행 시각과 발행한 사람이 남는다")
    void 초안_발행() {
        // given
        InquiryFaq faq = InquiryFaq.create(완성된_내용("재설정 메일을 보내요"), STAFF_ID);

        // when
        faq.publish(OTHER_STAFF_ID, true, NOW);

        // then
        assertThat(faq.getStatus()).isEqualTo(InquiryFaqStatus.PUBLISHED);
        assertThat(faq.getPublished()).isEqualTo(faq.getDraft());
        assertThat(faq.getPublishedAt()).isEqualTo(NOW);
        assertThat(faq.getPublishedByMemberId()).isEqualTo(OTHER_STAFF_ID);
        assertThat(faq.hasUnpublishedChanges()).isFalse();
        assertThat(faq.isPublicAt(NOW)).isTrue();
    }

    @Test
    @DisplayName("검증 체크가 빠지면 발행할 수 없다")
    void 검증_없이_발행() {
        InquiryFaq faq = InquiryFaq.create(완성된_내용("재설정 메일을 보내요"), STAFF_ID);

        assertThatThrownBy(() -> faq.publish(STAFF_ID, false, NOW))
            .extracting("baseCode")
            .isEqualTo(InquiryErrorCode.INQUIRY_FAQ_VERIFICATION_REQUIRED);
        assertThat(faq.getStatus()).isEqualTo(InquiryFaqStatus.DRAFT);
    }

    @Test
    @DisplayName("답변·카테고리·대상 중 하나라도 없으면 발행할 수 없다")
    void 미완성_발행() {
        InquiryFaq faq = InquiryFaq.create(InquiryFaqContent.draftOf("비밀번호를 잊었어요", 10L), STAFF_ID);

        assertThatThrownBy(() -> faq.publish(STAFF_ID, true, NOW))
            .extracting("baseCode")
            .isEqualTo(InquiryErrorCode.INQUIRY_FAQ_INCOMPLETE);
    }

    @Test
    @DisplayName("발행 중에 고치면 발행본은 그대로이고, 다시 발행하면 고친 내용이 발행본이 된다")
    void 발행_중_수정() {
        // given
        InquiryFaq faq = InquiryFaq.create(완성된_내용("재설정 메일을 보내요"), STAFF_ID);
        faq.publish(STAFF_ID, true, NOW);

        // when
        faq.saveDraft(완성된_내용("앱에서 재설정하세요"));

        // then
        assertThat(faq.hasUnpublishedChanges()).isTrue();
        assertThat(faq.getPublished().getAnswer()).isEqualTo("재설정 메일을 보내요");
        faq.publish(STAFF_ID, true, NOW.plusSeconds(60));
        assertThat(faq.hasUnpublishedChanges()).isFalse();
        assertThat(faq.getPublished().getAnswer()).isEqualTo("앱에서 재설정하세요");
    }

    @Test
    @DisplayName("한 번도 발행하지 않은 초안은 고쳐도 발행 전 변경이 있다고 보지 않는다")
    void 초안_수정() {
        InquiryFaq faq = InquiryFaq.create(InquiryFaqContent.draftOf("비밀번호를 잊었어요", null), STAFF_ID);

        faq.saveDraft(InquiryFaqContent.draftOf("비밀번호를 바꾸고 싶어요", null));

        assertThat(faq.hasUnpublishedChanges()).isFalse();
    }

    @Test
    @DisplayName("발행 중인 대표 질문만 보관할 수 있고, 보관하면 공개되지 않으며 다시 발행할 수 있다")
    void 보관과_재발행() {
        // given
        InquiryFaq faq = InquiryFaq.create(완성된_내용("재설정 메일을 보내요"), STAFF_ID);
        assertThatThrownBy(faq::archive)
            .extracting("baseCode")
            .isEqualTo(InquiryErrorCode.INQUIRY_FAQ_NOT_ARCHIVABLE);
        faq.publish(STAFF_ID, true, NOW);

        // when
        faq.archive();

        // then
        assertThat(faq.getStatus()).isEqualTo(InquiryFaqStatus.ARCHIVED);
        assertThat(faq.isPublicAt(NOW)).isFalse();
        assertThatThrownBy(faq::archive)
            .extracting("baseCode")
            .isEqualTo(InquiryErrorCode.INQUIRY_FAQ_NOT_ARCHIVABLE);
        faq.publish(STAFF_ID, true, NOW);
        assertThat(faq.getStatus()).isEqualTo(InquiryFaqStatus.PUBLISHED);
    }

    @Test
    @DisplayName("종료일은 한국 시간 그날 끝까지 공개하고 다음 날 0시부터 내린다")
    void 종료일_경계() {
        // given
        InquiryFaq faq = InquiryFaq.create(InquiryFaqContent.of(
            "행사 안내", "오늘까지예요", List.of(), 10L, 20L, LocalDate.of(2026, 11, 25), null
        ), STAFF_ID);
        faq.publish(STAFF_ID, true, NOW);

        // when & then
        assertThat(faq.isPublicAt(Instant.parse("2026-11-25T14:59:59Z"))).isTrue();
        assertThat(faq.isPublicAt(Instant.parse("2026-11-25T15:00:00Z"))).isFalse();
    }

    @Test
    @DisplayName("자기 자신을 관련 안내로 저장할 수 없다")
    void 자기_자신_관련_안내() {
        InquiryFaq faq = InquiryFaq.create(InquiryFaqContent.draftOf("비밀번호를 잊었어요", null), STAFF_ID);
        ReflectionTestUtils.setField(faq, "id", 7L);

        assertThatThrownBy(() -> faq.saveDraft(InquiryFaqContent.of(
            "비밀번호를 잊었어요", null, List.of(), null, null, null, 7L
        )))
            .extracting("baseCode")
            .isEqualTo(InquiryErrorCode.INVALID_INQUIRY_FAQ_RELATED);
    }

    private static InquiryFaqContent 완성된_내용(String answer) {
        return InquiryFaqContent.of("비밀번호를 잊었어요", answer, List.of(), 10L, 20L, null, null);
    }
}
