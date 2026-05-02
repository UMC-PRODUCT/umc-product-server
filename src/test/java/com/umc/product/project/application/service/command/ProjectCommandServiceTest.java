package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectCommand;
import com.umc.product.project.application.port.in.command.dto.SubmitProjectCommand;
import com.umc.product.project.application.port.in.command.dto.TransferProjectOwnershipCommand;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectCommand;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectCommandServiceTest {

    @Mock
    LoadProjectPort loadProjectPort;
    @Mock
    SaveProjectPort saveProjectPort;
    @Mock
    LoadProjectApplicationFormPort loadProjectApplicationFormPort;
    @Mock
    com.umc.product.project.application.port.out.LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    @Mock
    GetMemberUseCase getMemberUseCase;
    @Mock
    GetChallengerUseCase getChallengerUseCase;
    @Mock
    GetGisuUseCase getGisuUseCase;
    @Mock
    GetChapterUseCase getChapterUseCase;
    @Mock
    com.umc.product.survey.application.port.in.command.ManageFormUseCase manageFormUseCase;

    @InjectMocks
    ProjectCommandService sut;

    private Project createProject(ProjectStatus status) {
        Project project = Project.createDraft(1L, 2L, 100L, 7L);
        ReflectionTestUtils.setField(project, "id", 1L);
        ReflectionTestUtils.setField(project, "status", status);
        return project;
    }

    private ChallengerInfo challengerInfo(Long memberId, ChallengerPart part) {
        return ChallengerInfo.builder()
            .challengerId(1L)
            .memberId(memberId)
            .gisuId(1L)
            .part(part)
            .build();
    }

    private MemberInfo memberInfo(Long schoolId) {
        return MemberInfo.builder()
            .id(100L)
            .schoolId(schoolId)
            .build();
    }

    private GisuInfo gisuInfo() {
        return new GisuInfo(1L, 9L, null, null, true);
    }

    // --- helpers ---

    @Nested
    class create {

        @Test
        void 프로젝트_생성_성공() {
            var command = CreateDraftProjectCommand.builder()
                .gisuId(1L)
                .productOwnerMemberId(100L)
                .build();

            given(getGisuUseCase.getById(1L)).willReturn(gisuInfo());
            given(getChallengerUseCase.getByMemberIdAndGisuId(100L, 1L))
                .willReturn(challengerInfo(100L, ChallengerPart.PLAN));
            given(loadProjectPort.existsByOwnerAndGisu(100L, 1L)).willReturn(false);
            given(getMemberUseCase.getById(100L)).willReturn(memberInfo(10L));
            given(getChapterUseCase.byGisuAndSchool(1L, 10L)).willReturn(new ChapterInfo(5L, "서울"));
            given(saveProjectPort.save(any())).willAnswer(inv -> {
                Project p = inv.getArgument(0);
                ReflectionTestUtils.setField(p, "id", 99L);
                return p;
            });

            Long result = sut.create(command);

            assertThat(result).isEqualTo(99L);
            then(saveProjectPort).should().save(any(Project.class));
        }

        @Test
        void PLAN_파트가_아니면_예외() {
            var command = CreateDraftProjectCommand.builder()
                .gisuId(1L)
                .productOwnerMemberId(100L)
                .build();

            given(getGisuUseCase.getById(1L)).willReturn(gisuInfo());
            given(getChallengerUseCase.getByMemberIdAndGisuId(100L, 1L))
                .willReturn(challengerInfo(100L, ChallengerPart.SPRINGBOOT));

            assertThatThrownBy(() -> sut.create(command))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_OWNER_NOT_PLAN_CHALLENGER);
        }

        @Test
        void 중복_프로젝트면_예외() {
            var command = CreateDraftProjectCommand.builder()
                .gisuId(1L)
                .productOwnerMemberId(100L)
                .build();

            given(getGisuUseCase.getById(1L)).willReturn(gisuInfo());
            given(getChallengerUseCase.getByMemberIdAndGisuId(100L, 1L))
                .willReturn(challengerInfo(100L, ChallengerPart.PLAN));
            given(loadProjectPort.existsByOwnerAndGisu(100L, 1L)).willReturn(true);

            assertThatThrownBy(() -> sut.create(command))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_DUPLICATE_IN_GISU);
        }
    }

    @Nested
    class update {

        @Test
        void DRAFT_상태_수정_성공() {
            Project project = createProject(ProjectStatus.DRAFT);
            given(loadProjectPort.getById(1L)).willReturn(project);

            sut.update(updateCommand("새이름"));

            assertThat(project.getName()).isEqualTo("새이름");
        }

        @Test
        void PENDING_REVIEW_상태_수정_성공() {
            Project project = createProject(ProjectStatus.PENDING_REVIEW);
            given(loadProjectPort.getById(1L)).willReturn(project);

            sut.update(updateCommand("새이름"));

            assertThat(project.getName()).isEqualTo("새이름");
        }

        @Test
        void IN_PROGRESS_상태_수정_성공() {
            Project project = createProject(ProjectStatus.IN_PROGRESS);
            given(loadProjectPort.getById(1L)).willReturn(project);

            sut.update(updateCommand("새이름"));

            assertThat(project.getName()).isEqualTo("새이름");
        }

        @Test
        void COMPLETED_상태는_수정_불가() {
            Project project = createProject(ProjectStatus.COMPLETED);
            given(loadProjectPort.getById(1L)).willReturn(project);

            assertThatThrownBy(() -> sut.update(updateCommand("새이름")))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_INVALID_STATE);
        }

        @Test
        void ABORTED_상태는_수정_불가() {
            Project project = createProject(ProjectStatus.ABORTED);
            given(loadProjectPort.getById(1L)).willReturn(project);

            assertThatThrownBy(() -> sut.update(updateCommand("새이름")))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_INVALID_STATE);
        }

        private UpdateProjectCommand updateCommand(String name) {
            return UpdateProjectCommand.builder()
                .projectId(1L)
                .requesterMemberId(100L)
                .name(name)
                .build();
        }
    }

    @Nested
    class submit {

        @Test
        void 프로젝트_제출_성공() {
            Project project = createProject(ProjectStatus.DRAFT);
            project.updateBasicInfo("프로젝트명", null, null, null, null);
            given(loadProjectPort.getById(1L)).willReturn(project);
            given(loadProjectApplicationFormPort.existsByProjectId(1L)).willReturn(true);

            sut.submit(SubmitProjectCommand.builder()
                .projectId(1L)
                .requesterMemberId(100L)
                .build());

            assertThat(project.getStatus()).isEqualTo(ProjectStatus.PENDING_REVIEW);
        }

        @Test
        void 작성자가_아니면_예외() {
            Project project = createProject(ProjectStatus.DRAFT);
            given(loadProjectPort.getById(1L)).willReturn(project);

            assertThatThrownBy(() -> sut.submit(SubmitProjectCommand.builder()
                .projectId(1L)
                .requesterMemberId(999L)
                .build()))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }

        @Test
        void 지원폼_미연결이면_SUBMIT_VALIDATION_FAILED() {
            Project project = createProject(ProjectStatus.DRAFT);
            project.updateBasicInfo("프로젝트명", null, null, null, null);
            given(loadProjectPort.getById(1L)).willReturn(project);
            given(loadProjectApplicationFormPort.existsByProjectId(1L)).willReturn(false);

            assertThatThrownBy(() -> sut.submit(SubmitProjectCommand.builder()
                .projectId(1L)
                .requesterMemberId(100L)
                .build()))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_SUBMIT_VALIDATION_FAILED);
        }
    }

    @Nested
    class transferOwnership {

        @Test
        void 양도_성공() {
            Project project = createProject(ProjectStatus.DRAFT);
            given(loadProjectPort.getById(1L)).willReturn(project);
            given(getChallengerUseCase.getByMemberIdAndGisuId(200L, 1L))
                .willReturn(challengerInfo(200L, ChallengerPart.PLAN));
            given(loadProjectPort.existsByOwnerAndGisu(200L, 1L)).willReturn(false);
            given(getMemberUseCase.getById(200L)).willReturn(memberInfo(8L));

            sut.transfer(transferCommand(100L, 200L));

            assertThat(project.getProductOwnerMemberId()).isEqualTo(200L);
            assertThat(project.getProductOwnerSchoolId()).isEqualTo(8L);
        }

        @Test
        void 현재_PM이_아니면_예외() {
            Project project = createProject(ProjectStatus.DRAFT);
            given(loadProjectPort.getById(1L)).willReturn(project);

            assertThatThrownBy(() -> sut.transfer(transferCommand(999L, 200L)))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }

        @Test
        void 새_PM이_PLAN이_아니면_예외() {
            Project project = createProject(ProjectStatus.DRAFT);
            given(loadProjectPort.getById(1L)).willReturn(project);
            given(getChallengerUseCase.getByMemberIdAndGisuId(200L, 1L))
                .willReturn(challengerInfo(200L, ChallengerPart.WEB));

            assertThatThrownBy(() -> sut.transfer(transferCommand(100L, 200L)))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_OWNER_NOT_PLAN_CHALLENGER);
        }

        @Test
        void 새_PM이_같은_기수에_이미_프로젝트_있으면_예외() {
            Project project = createProject(ProjectStatus.DRAFT);
            given(loadProjectPort.getById(1L)).willReturn(project);
            given(getChallengerUseCase.getByMemberIdAndGisuId(200L, 1L))
                .willReturn(challengerInfo(200L, ChallengerPart.PLAN));
            given(loadProjectPort.existsByOwnerAndGisu(200L, 1L)).willReturn(true);

            assertThatThrownBy(() -> sut.transfer(transferCommand(100L, 200L)))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_DUPLICATE_IN_GISU);
        }

        @Test
        void COMPLETED_상태는_양도_불가() {
            Project project = createProject(ProjectStatus.COMPLETED);
            given(loadProjectPort.getById(1L)).willReturn(project);
            given(getChallengerUseCase.getByMemberIdAndGisuId(200L, 1L))
                .willReturn(challengerInfo(200L, ChallengerPart.PLAN));
            given(loadProjectPort.existsByOwnerAndGisu(200L, 1L)).willReturn(false);

            assertThatThrownBy(() -> sut.transfer(transferCommand(100L, 200L)))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_INVALID_STATE);
        }

        private TransferProjectOwnershipCommand transferCommand(Long requester, Long newOwner) {
            return TransferProjectOwnershipCommand.builder()
                .projectId(1L)
                .requesterMemberId(requester)
                .newOwnerMemberId(newOwner)
                .build();
        }
    }

    @Nested
    class publish {

        @Test
        void PENDING_REVIEW에서_IN_PROGRESS로_전이_및_Form_동반_publish() {
            Project project = createProject(ProjectStatus.PENDING_REVIEW);
            given(loadProjectPort.getById(1L)).willReturn(project);
            given(loadProjectPartQuotaPort.listByProjectId(1L)).willReturn(java.util.List.of(
                com.umc.product.project.domain.ProjectPartQuota.create(
                    project, ChallengerPart.WEB, 3L, 99L)
            ));
            com.umc.product.project.domain.ProjectApplicationForm form =
                com.umc.product.project.domain.ProjectApplicationForm.create(project, 500L);
            given(loadProjectApplicationFormPort.findByProjectId(1L))
                .willReturn(java.util.Optional.of(form));

            ProjectStatus status = sut.publish(
                com.umc.product.project.application.port.in.command.dto.PublishProjectCommand.builder()
                    .projectId(1L).requesterMemberId(99L).build());

            assertThat(status).isEqualTo(ProjectStatus.IN_PROGRESS);
            org.mockito.BDDMockito.then(manageFormUseCase).should().publishForm(any());
        }

        @Test
        void DRAFT_상태는_publish_불가() {
            Project project = createProject(ProjectStatus.DRAFT);
            given(loadProjectPort.getById(1L)).willReturn(project);
            given(loadProjectPartQuotaPort.listByProjectId(1L)).willReturn(java.util.List.of(
                com.umc.product.project.domain.ProjectPartQuota.create(
                    project, ChallengerPart.WEB, 3L, 99L)
            ));
            com.umc.product.project.domain.ProjectApplicationForm form =
                com.umc.product.project.domain.ProjectApplicationForm.create(project, 500L);
            given(loadProjectApplicationFormPort.findByProjectId(1L))
                .willReturn(java.util.Optional.of(form));

            assertThatThrownBy(() -> sut.publish(
                com.umc.product.project.application.port.in.command.dto.PublishProjectCommand.builder()
                    .projectId(1L).requesterMemberId(99L).build()))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_INVALID_STATE);
        }

        @Test
        void 파트_quota가_없으면_거부() {
            Project project = createProject(ProjectStatus.PENDING_REVIEW);
            given(loadProjectPort.getById(1L)).willReturn(project);
            given(loadProjectPartQuotaPort.listByProjectId(1L)).willReturn(java.util.List.of());

            assertThatThrownBy(() -> sut.publish(
                com.umc.product.project.application.port.in.command.dto.PublishProjectCommand.builder()
                    .projectId(1L).requesterMemberId(99L).build()))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_PART_QUOTA_REQUIRED);
        }

        @Test
        void 지원_폼이_없으면_거부() {
            Project project = createProject(ProjectStatus.PENDING_REVIEW);
            given(loadProjectPort.getById(1L)).willReturn(project);
            given(loadProjectPartQuotaPort.listByProjectId(1L)).willReturn(java.util.List.of(
                com.umc.product.project.domain.ProjectPartQuota.create(
                    project, ChallengerPart.WEB, 3L, 99L)
            ));
            given(loadProjectApplicationFormPort.findByProjectId(1L))
                .willReturn(java.util.Optional.empty());

            assertThatThrownBy(() -> sut.publish(
                com.umc.product.project.application.port.in.command.dto.PublishProjectCommand.builder()
                    .projectId(1L).requesterMemberId(99L).build()))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.APPLICATION_FORM_NOT_FOUND);
        }
    }
}
