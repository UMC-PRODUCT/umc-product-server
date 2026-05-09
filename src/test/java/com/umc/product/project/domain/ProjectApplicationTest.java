package com.umc.product.project.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ProjectApplicationTest {

    private static final Long APPLICANT_MEMBER_ID = 100L;
    private static final Long DECIDER_MEMBER_ID = 200L;
    private static final Long FORM_RESPONSE_ID = 300L;

    /**
     * 매칭 차수가 진행 중이라고 가정하기 위해 시작/마감을 현재 시각 기준 ±1일로 설정한다.
     */
    private static final Instant NOW = Instant.now();
    private static final Instant ROUND_STARTS_AT = NOW.minusSeconds(86_400);
    private static final Instant ROUND_ENDS_AT = NOW.plusSeconds(43_200);
    private static final Instant ROUND_DECISION_DEADLINE = NOW.plusSeconds(86_400);

    ProjectApplication application;
    ProjectMatchingRound round;

    @BeforeEach
    void setUp() {
        round = openRound();
        application = ProjectApplication.create(applicationForm(), FORM_RESPONSE_ID, APPLICANT_MEMBER_ID, round);
        setStatus(application, ProjectApplicationStatus.SUBMITTED);
    }

    @Nested
    class approve {

        @Test
        void SUBMITTED를_APPROVED로_전이한다() {
            application.approve(DECIDER_MEMBER_ID, "역량 우수");

            assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.APPROVED);
            assertThat(application.getStatusChangedMemberId()).isEqualTo(DECIDER_MEMBER_ID);
            assertThat(application.getStatusChangeReason()).isEqualTo("역량 우수");
        }

        @Test
        void REJECTED를_APPROVED로_재토글한다() {
            setStatus(application, ProjectApplicationStatus.REJECTED);

            application.approve(DECIDER_MEMBER_ID, null);

            assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.APPROVED);
        }

        @Test
        void APPROVED에서_같은_status로_호출해도_예외_없이_갱신된다() {
            setStatus(application, ProjectApplicationStatus.APPROVED);

            assertThatCode(() -> application.approve(DECIDER_MEMBER_ID, "재확인"))
                .doesNotThrowAnyException();
            assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.APPROVED);
        }

        @Test
        void DRAFT_상태에서는_PROJECT_APPLICATION_DECISION_INVALID_TRANSITION() {
            setStatus(application, ProjectApplicationStatus.DRAFT);

            assertThatThrownBy(() -> application.approve(DECIDER_MEMBER_ID, null))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_APPLICATION_DECISION_INVALID_TRANSITION);
        }

        @Test
        void 차수_종료_후에는_PROJECT_MATCHING_ROUND_LOCKED() {
            setRoundDeadline(round, NOW.minusSeconds(60));

            assertThatThrownBy(() -> application.approve(DECIDER_MEMBER_ID, null))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_LOCKED);
        }
    }

    @Nested
    class reject {

        @Test
        void SUBMITTED를_REJECTED로_전이한다() {
            application.reject(DECIDER_MEMBER_ID, "면접 결과 부적합");

            assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.REJECTED);
            assertThat(application.getStatusChangedMemberId()).isEqualTo(DECIDER_MEMBER_ID);
            assertThat(application.getStatusChangeReason()).isEqualTo("면접 결과 부적합");
        }

        @Test
        void APPROVED를_REJECTED로_재토글한다() {
            setStatus(application, ProjectApplicationStatus.APPROVED);

            application.reject(DECIDER_MEMBER_ID, null);

            assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.REJECTED);
        }

        @Test
        void DRAFT_상태에서는_PROJECT_APPLICATION_DECISION_INVALID_TRANSITION() {
            setStatus(application, ProjectApplicationStatus.DRAFT);

            assertThatThrownBy(() -> application.reject(DECIDER_MEMBER_ID, null))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_APPLICATION_DECISION_INVALID_TRANSITION);
        }

        @Test
        void 차수_종료_후에는_PROJECT_MATCHING_ROUND_LOCKED() {
            setRoundDeadline(round, NOW.minusSeconds(60));

            assertThatThrownBy(() -> application.reject(DECIDER_MEMBER_ID, null))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_LOCKED);
        }
    }

    @Nested
    class revertToPending {

        @Test
        void APPROVED를_SUBMITTED로_되돌리고_사유는_초기화한다() {
            setStatus(application, ProjectApplicationStatus.APPROVED);
            ReflectionTestUtils.setField(application, "statusChangeReason", "이전 사유");

            application.revertToPending(DECIDER_MEMBER_ID);

            assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.SUBMITTED);
            assertThat(application.getStatusChangedMemberId()).isEqualTo(DECIDER_MEMBER_ID);
            assertThat(application.getStatusChangeReason()).isNull();
        }

        @Test
        void REJECTED를_SUBMITTED로_되돌린다() {
            setStatus(application, ProjectApplicationStatus.REJECTED);

            application.revertToPending(DECIDER_MEMBER_ID);

            assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.SUBMITTED);
        }

        @Test
        void SUBMITTED_상태에서는_PROJECT_APPLICATION_DECISION_INVALID_TRANSITION() {
            assertThatThrownBy(() -> application.revertToPending(DECIDER_MEMBER_ID))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_APPLICATION_DECISION_INVALID_TRANSITION);
        }

        @Test
        void DRAFT_상태에서는_PROJECT_APPLICATION_DECISION_INVALID_TRANSITION() {
            setStatus(application, ProjectApplicationStatus.DRAFT);

            assertThatThrownBy(() -> application.revertToPending(DECIDER_MEMBER_ID))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_APPLICATION_DECISION_INVALID_TRANSITION);
        }

        @Test
        void 차수_종료_후에는_PROJECT_MATCHING_ROUND_LOCKED() {
            setStatus(application, ProjectApplicationStatus.APPROVED);
            setRoundDeadline(round, NOW.minusSeconds(60));

            assertThatThrownBy(() -> application.revertToPending(DECIDER_MEMBER_ID))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_LOCKED);
        }
    }

    private void setStatus(ProjectApplication application, ProjectApplicationStatus status) {
        ReflectionTestUtils.setField(application, "status", status);
    }

    private void setRoundDeadline(ProjectMatchingRound round, Instant deadline) {
        ReflectionTestUtils.setField(round, "decisionDeadline", deadline);
    }

    private ProjectMatchingRound openRound() {
        return ProjectMatchingRound.create(
            "기획-디자인 1차 매칭", null,
            MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, 1L,
            ROUND_STARTS_AT, ROUND_ENDS_AT, ROUND_DECISION_DEADLINE
        );
    }

    private ProjectApplicationForm applicationForm() {
        Project project = Project.createDraft(1L, 2L, 999L, 7L, 999L);
        return ProjectApplicationForm.create(project, 500L);
    }
}
