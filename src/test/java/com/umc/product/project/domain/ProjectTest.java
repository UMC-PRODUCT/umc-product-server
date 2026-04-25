package com.umc.product.project.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProjectTest {

    Project project;

    @BeforeEach
    void setUp() {
        project = Project.createDraft(1L, 2L, 100L);
    }

    @Nested
    class createDraft {

        @Test
        void DRAFT_상태로_생성된다() {
            assertThat(project.getStatus()).isEqualTo(ProjectStatus.DRAFT);
        }

        @Test
        void 기수_지부_PO가_설정된다() {
            assertThat(project.getGisuId()).isEqualTo(1L);
            assertThat(project.getChapterId()).isEqualTo(2L);
            assertThat(project.getProductOwnerMemberId()).isEqualTo(100L);
        }

        @Test
        void name과_description은_null이다() {
            assertThat(project.getName()).isNull();
            assertThat(project.getDescription()).isNull();
        }
    }

    @Nested
    class updateBasicInfo {

        @Test
        void null이_아닌_필드만_업데이트된다() {
            project.updateBasicInfo("프로젝트A", null, null, null, null);

            assertThat(project.getName()).isEqualTo("프로젝트A");
            assertThat(project.getDescription()).isNull();
        }

        @Test
        void 모든_기본정보_필드를_업데이트할_수_있다() {
            project.updateBasicInfo("이름", "설명", "https://link.com", "thumb-uuid", "logo-uuid");

            assertThat(project.getName()).isEqualTo("이름");
            assertThat(project.getDescription()).isEqualTo("설명");
            assertThat(project.getExternalLink()).isEqualTo("https://link.com");
            assertThat(project.getThumbnailFileId()).isEqualTo("thumb-uuid");
            assertThat(project.getLogoFileId()).isEqualTo("logo-uuid");
        }

        @Test
        void 전부_null이면_아무것도_변경되지_않는다() {
            project.updateBasicInfo("원래이름", null, null, null, null);

            project.updateBasicInfo(null, null, null, null, null);

            assertThat(project.getName()).isEqualTo("원래이름");
        }

        @Test
        void COMPLETED_상태에서는_PROJECT_INVALID_STATE() {
            setStatus(project, ProjectStatus.COMPLETED);

            assertThatThrownBy(() -> project.updateBasicInfo("새이름", null, null, null, null))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_INVALID_STATE);
        }

        @Test
        void ABORTED_상태에서는_PROJECT_INVALID_STATE() {
            setStatus(project, ProjectStatus.ABORTED);

            assertThatThrownBy(() -> project.updateBasicInfo("새이름", null, null, null, null))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_INVALID_STATE);
        }
    }

    @Nested
    class transferOwnership {

        @Test
        void 새_PM에게_양도된다() {
            project.transferOwnership(200L);

            assertThat(project.getProductOwnerMemberId()).isEqualTo(200L);
        }

        @Test
        void COMPLETED_상태에서는_PROJECT_INVALID_STATE() {
            setStatus(project, ProjectStatus.COMPLETED);

            assertThatThrownBy(() -> project.transferOwnership(200L))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_INVALID_STATE);
        }

        @Test
        void ABORTED_상태에서는_PROJECT_INVALID_STATE() {
            setStatus(project, ProjectStatus.ABORTED);

            assertThatThrownBy(() -> project.transferOwnership(200L))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_INVALID_STATE);
        }
    }

    @Nested
    class submit {

        @Test
        void DRAFT에서_PENDING_REVIEW로_전이된다() {
            project.updateBasicInfo("프로젝트명", null, null, null, null);
            project.attachApplicationForm(10L);

            project.submit();

            assertThat(project.getStatus()).isEqualTo(ProjectStatus.PENDING_REVIEW);
        }

        @Test
        void DRAFT가_아니면_PROJECT_INVALID_STATE() {
            project.updateBasicInfo("프로젝트명", null, null, null, null);
            project.attachApplicationForm(10L);
            project.submit();

            assertThatThrownBy(() -> project.submit())
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_INVALID_STATE);
        }

        @Test
        void name이_null이면_SUBMIT_VALIDATION_FAILED() {
            project.attachApplicationForm(10L);

            assertThatThrownBy(() -> project.submit())
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_SUBMIT_VALIDATION_FAILED);
        }

        @Test
        void name이_빈문자열이면_SUBMIT_VALIDATION_FAILED() {
            project.updateBasicInfo("  ", null, null, null, null);
            project.attachApplicationForm(10L);

            assertThatThrownBy(() -> project.submit())
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_SUBMIT_VALIDATION_FAILED);
        }

        @Test
        void applicationFormId가_null이면_SUBMIT_VALIDATION_FAILED() {
            project.updateBasicInfo("프로젝트명", null, null, null, null);

            assertThatThrownBy(() -> project.submit())
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_SUBMIT_VALIDATION_FAILED);
        }
    }

    @Nested
    class attachApplicationForm {

        @Test
        void applicationFormId가_설정된다() {
            project.attachApplicationForm(42L);

            assertThat(project.getApplicationFormId()).isEqualTo(42L);
        }
    }

    @Nested
    class complete {

        @Test
        void IN_PROGRESS에서_COMPLETED로_전이된다() {
            setStatus(project, ProjectStatus.IN_PROGRESS);

            project.complete();

            assertThat(project.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
        }

        @Test
        void IN_PROGRESS가_아니면_PROJECT_INVALID_STATE() {
            assertThatThrownBy(() -> project.complete())
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_INVALID_STATE);
        }
    }

    @Nested
    class abort {

        @Test
        void DRAFT에서_ABORTED로_전이된다() {
            project.abort("사유", 999L);

            assertThat(project.getStatus()).isEqualTo(ProjectStatus.ABORTED);
            assertThat(project.getStatusChangedReason()).isEqualTo("사유");
            assertThat(project.getStatusChangedByMemberId()).isEqualTo(999L);
        }

        @Test
        void PENDING_REVIEW에서_ABORTED로_전이된다() {
            project.updateBasicInfo("이름", null, null, null, null);
            project.attachApplicationForm(10L);
            project.submit();

            project.abort("사유", 999L);

            assertThat(project.getStatus()).isEqualTo(ProjectStatus.ABORTED);
        }

        @Test
        void COMPLETED_상태에서는_abort_불가() {
            setStatus(project, ProjectStatus.COMPLETED);

            assertThatThrownBy(() -> project.abort("사유", 999L))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_ABORT_UNAVAILABLE);
        }

        @Test
        void ABORTED_상태에서는_abort_불가() {
            project.abort("첫번째 사유", 999L);

            assertThatThrownBy(() -> project.abort("두번째 사유", 888L))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_ABORT_UNAVAILABLE);
        }
    }

    private void setStatus(Project project, ProjectStatus status) {
        try {
            var field = Project.class.getDeclaredField("status");
            field.setAccessible(true);
            field.set(project, status);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
