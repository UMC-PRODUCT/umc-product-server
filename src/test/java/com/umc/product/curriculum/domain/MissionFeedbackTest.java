package com.umc.product.curriculum.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.curriculum.domain.enums.FeedbackResult;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;

@DisplayName("미션 피드백 도메인")
class MissionFeedbackTest {

    private static final Long REVIEWER_MEMBER_ID = 7L;

    @Test
    @DisplayName("미션 제출물에 대한 피드백을 생성한다")
    void createMissionFeedbackSuccess() {
        // given
        MissionSubmission submission = mock(MissionSubmission.class);

        // when
        MissionFeedback feedback = MissionFeedback.create(
            submission,
            REVIEWER_MEMBER_ID,
            "잘 제출했습니다.",
            FeedbackResult.PASS
        );

        // then
        assertThat(feedback.getMissionSubmission()).isSameAs(submission);
        assertThat(feedback.getReviewerMemberId()).isEqualTo(REVIEWER_MEMBER_ID);
        assertThat(feedback.getContent()).isEqualTo("잘 제출했습니다.");
        assertThat(feedback.getFeedbackResult()).isEqualTo(FeedbackResult.PASS);
    }

    @Test
    @DisplayName("피드백 내용이 없으면 생성할 수 없다")
    void createMissionFeedbackWithoutContentFails() {
        // given
        MissionSubmission submission = mock(MissionSubmission.class);

        // when & then
        assertThatThrownBy(() -> MissionFeedback.create(
            submission,
            REVIEWER_MEMBER_ID,
            " ",
            FeedbackResult.FAIL
        ))
            .isInstanceOf(CurriculumDomainException.class)
            .extracting("baseCode")
            .isEqualTo(CurriculumErrorCode.FEEDBACK_REQUIRED);
    }

    @Test
    @DisplayName("피드백 내용을 수정한다")
    void editMissionFeedbackContent() {
        // given
        MissionFeedback feedback = MissionFeedback.create(
            mock(MissionSubmission.class),
            REVIEWER_MEMBER_ID,
            "기존 피드백",
            FeedbackResult.FAIL
        );

        // when
        feedback.edit("수정된 피드백");

        // then
        assertThat(feedback.getContent()).isEqualTo("수정된 피드백");
    }

    @Test
    @DisplayName("피드백 작성자 본인 여부를 확인한다")
    void checkMissionFeedbackReviewer() {
        // given
        MissionFeedback feedback = MissionFeedback.create(
            mock(MissionSubmission.class),
            REVIEWER_MEMBER_ID,
            "피드백",
            FeedbackResult.PASS
        );

        // when & then
        assertThat(feedback.isReviewedBy(REVIEWER_MEMBER_ID)).isTrue();
        assertThat(feedback.isReviewedBy(999L)).isFalse();
    }
}
