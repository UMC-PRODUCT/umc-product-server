package com.umc.product.curriculum.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;

@DisplayName("미션 제출 도메인")
class MissionSubmissionTest {

    private static final Long REQUESTER_MEMBER_ID = 1L;

    @Test
    @DisplayName("원본 미션의 제출 유형으로 제출물을 생성한다")
    void createMissionSubmissionWithOriginalMissionType() {
        // given
        OriginalWorkbookMission mission = mission(MissionType.LINK);
        ChallengerWorkbook workbook = challengerWorkbook(REQUESTER_MEMBER_ID);

        // when
        MissionSubmission submission = MissionSubmission.create(mission, workbook, "https://github.com/umc/product");

        // then
        assertThat(submission.getOriginalWorkbookMission()).isSameAs(mission);
        assertThat(submission.getChallengerWorkbook()).isSameAs(workbook);
        assertThat(submission.getSubmittedAsType()).isEqualTo(MissionType.LINK);
        assertThat(submission.getContent()).isEqualTo("https://github.com/umc/product");
    }

    @Test
    @DisplayName("링크 미션은 제출 내용이 없으면 생성할 수 없다")
    void createLinkMissionSubmissionWithoutContentFails() {
        // given
        OriginalWorkbookMission mission = mission(MissionType.LINK);
        ChallengerWorkbook workbook = challengerWorkbook(REQUESTER_MEMBER_ID);

        // when & then
        assertThatThrownBy(() -> MissionSubmission.create(mission, workbook, " "))
            .isInstanceOf(CurriculumDomainException.class)
            .extracting("baseCode")
            .isEqualTo(CurriculumErrorCode.SUBMISSION_REQUIRED);
    }

    @Test
    @DisplayName("제출 내용을 수정한다")
    void editMissionSubmissionContent() {
        // given
        MissionSubmission submission = MissionSubmission.create(
            mission(MissionType.MEMO),
            challengerWorkbook(REQUESTER_MEMBER_ID),
            "기존 내용"
        );

        // when
        submission.edit("수정된 내용");

        // then
        assertThat(submission.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("제출자 본인 여부를 확인한다")
    void checkMissionSubmissionOwner() {
        // given
        MissionSubmission submission = MissionSubmission.create(
            mission(MissionType.PLAIN),
            challengerWorkbook(REQUESTER_MEMBER_ID),
            null
        );

        // when & then
        assertThat(submission.isSubmittedBy(REQUESTER_MEMBER_ID)).isTrue();
        assertThat(submission.isSubmittedBy(999L)).isFalse();
    }

    private OriginalWorkbookMission mission(MissionType missionType) {
        OriginalWorkbookMission mission = mock(OriginalWorkbookMission.class);
        given(mission.getMissionType()).willReturn(missionType);
        return mission;
    }

    private ChallengerWorkbook challengerWorkbook(Long memberId) {
        ChallengerWorkbook workbook = mock(ChallengerWorkbook.class);
        given(workbook.getMemberId()).willReturn(memberId);
        return workbook;
    }
}
