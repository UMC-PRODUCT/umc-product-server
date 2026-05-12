package com.umc.product.project.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ProjectApplicationTest {

    private static final Long APPLICANT_ID = 100L;
    private static final Long DECIDER_ID = 100L;

    @Nested
    class cancel {

        @Test
        void DRAFT에서_CANCELLED로_전이된다() {
            ProjectApplication application = applicationWithStatus(ProjectApplicationStatus.DRAFT);

            application.cancel(DECIDER_ID, "임시저장본 더 안 쓸래요");

            assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.CANCELLED);
            assertThat(application.isCancelled()).isTrue();
            assertThat(application.getStatusChangedMemberId()).isEqualTo(DECIDER_ID);
            assertThat(application.getStatusChangeReason()).isEqualTo("임시저장본 더 안 쓸래요");
        }

        @Test
        void SUBMITTED에서_CANCELLED로_전이된다() {
            ProjectApplication application = applicationWithStatus(ProjectApplicationStatus.SUBMITTED);

            application.cancel(DECIDER_ID, "다른 프로젝트로 갈래요");

            assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.CANCELLED);
            assertThat(application.getStatusChangeReason()).isEqualTo("다른 프로젝트로 갈래요");
        }

        @Test
        void reason은_null이어도_허용된다() {
            ProjectApplication application = applicationWithStatus(ProjectApplicationStatus.DRAFT);

            application.cancel(DECIDER_ID, null);

            assertThat(application.getStatus()).isEqualTo(ProjectApplicationStatus.CANCELLED);
            assertThat(application.getStatusChangeReason()).isNull();
        }

        @Test
        void APPROVED_상태에서는_CANCEL_NOT_ALLOWED() {
            ProjectApplication application = applicationWithStatus(ProjectApplicationStatus.APPROVED);

            assertThatThrownBy(() -> application.cancel(DECIDER_ID, "사유"))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_APPLICATION_CANCEL_NOT_ALLOWED);
        }

        @Test
        void REJECTED_상태에서는_CANCEL_NOT_ALLOWED() {
            ProjectApplication application = applicationWithStatus(ProjectApplicationStatus.REJECTED);

            assertThatThrownBy(() -> application.cancel(DECIDER_ID, "사유"))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_APPLICATION_CANCEL_NOT_ALLOWED);
        }

        @Test
        void 이미_CANCELLED_상태에서는_CANCEL_NOT_ALLOWED() {
            ProjectApplication application = applicationWithStatus(ProjectApplicationStatus.CANCELLED);

            assertThatThrownBy(() -> application.cancel(DECIDER_ID, "사유"))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_APPLICATION_CANCEL_NOT_ALLOWED);
        }
    }

    private ProjectApplication applicationWithStatus(ProjectApplicationStatus status) {
        try {
            var constructor = ProjectApplication.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            ProjectApplication application = constructor.newInstance();
            ReflectionTestUtils.setField(application, "id", 1L);
            ReflectionTestUtils.setField(application, "applicantMemberId", APPLICANT_ID);
            ReflectionTestUtils.setField(application, "status", status);
            return application;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
