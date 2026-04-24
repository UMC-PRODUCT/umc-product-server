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
import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import com.umc.product.project.application.port.in.command.dto.CreateDraftProjectCommand;
import com.umc.product.project.application.port.in.command.dto.SubmitProjectCommand;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectCommand;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectPort;
import com.umc.product.project.domain.Project;
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

    @Mock LoadProjectPort loadProjectPort;
    @Mock SaveProjectPort saveProjectPort;
    @Mock GetMemberUseCase getMemberUseCase;
    @Mock GetChallengerUseCase getChallengerUseCase;
    @Mock GetGisuUseCase getGisuUseCase;
    @Mock GetChapterUseCase getChapterUseCase;

    @InjectMocks ProjectCommandService sut;

    @Nested
    class create {

        @Test
        void 프로젝트_생성_성공() {
            // given
            var command = CreateDraftProjectCommand.builder()
                .gisuId(1L)
                .productOwnerMemberId(100L)
                .build();

            given(getGisuUseCase.getById(1L)).willReturn(gisuInfo());
            given(getChallengerUseCase.getByMemberIdAndGisuId(100L, 1L))
                .willReturn(challengerInfo(ChallengerPart.PLAN));
            given(loadProjectPort.existsByOwnerAndGisu(100L, 1L)).willReturn(false);
            given(getMemberUseCase.getById(100L)).willReturn(memberInfo(10L));
            given(getChapterUseCase.byGisuAndSchool(1L, 10L)).willReturn(new ChapterInfo(5L, "서울"));
            given(saveProjectPort.save(any())).willAnswer(inv -> {
                Project p = inv.getArgument(0);
                ReflectionTestUtils.setField(p, "id", 99L);
                return p;
            });

            // when
            Long result = sut.create(command);

            // then
            assertThat(result).isEqualTo(99L);
            then(saveProjectPort).should().save(any(Project.class));
        }

        @Test
        void PLAN_파트가_아니면_예외() {
            // given
            var command = CreateDraftProjectCommand.builder()
                .gisuId(1L)
                .productOwnerMemberId(100L)
                .build();

            given(getGisuUseCase.getById(1L)).willReturn(gisuInfo());
            given(getChallengerUseCase.getByMemberIdAndGisuId(100L, 1L))
                .willReturn(challengerInfo(ChallengerPart.SPRINGBOOT));

            // when & then
            assertThatThrownBy(() -> sut.create(command))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_OWNER_NOT_PLAN_CHALLENGER);
        }

        @Test
        void 중복_프로젝트면_예외() {
            // given
            var command = CreateDraftProjectCommand.builder()
                .gisuId(1L)
                .productOwnerMemberId(100L)
                .build();

            given(getGisuUseCase.getById(1L)).willReturn(gisuInfo());
            given(getChallengerUseCase.getByMemberIdAndGisuId(100L, 1L))
                .willReturn(challengerInfo(ChallengerPart.PLAN));
            given(loadProjectPort.existsByOwnerAndGisu(100L, 1L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> sut.create(command))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_DUPLICATE_IN_GISU);
        }
    }

    @Nested
    class update {

        @Test
        void 프로젝트_수정_성공() {
            // given
            Project project = createDraftProject(1L, 100L);
            given(loadProjectPort.getById(1L)).willReturn(project);

            var command = UpdateProjectCommand.builder()
                .projectId(1L)
                .requesterMemberId(100L)
                .name("새이름")
                .build();

            // when
            sut.update(command);

            // then
            assertThat(project.getName()).isEqualTo("새이름");
        }

        @Test
        void 작성자가_아니면_예외() {
            // given
            Project project = createDraftProject(1L, 100L);
            given(loadProjectPort.getById(1L)).willReturn(project);

            var command = UpdateProjectCommand.builder()
                .projectId(1L)
                .requesterMemberId(999L)
                .name("새이름")
                .build();

            // when & then
            assertThatThrownBy(() -> sut.update(command))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }
    }

    @Nested
    class submit {

        @Test
        void 프로젝트_제출_성공() {
            // given
            Project project = createDraftProject(1L, 100L);
            project.updateBasicInfo("프로젝트명", null, null, null, null, null);
            project.attachApplicationForm(10L);
            given(loadProjectPort.getById(1L)).willReturn(project);

            var command = SubmitProjectCommand.builder()
                .projectId(1L)
                .requesterMemberId(100L)
                .build();

            // when
            sut.submit(command);

            // then
            assertThat(project.getStatus())
                .isEqualTo(com.umc.product.project.domain.enums.ProjectStatus.PENDING_REVIEW);
        }

        @Test
        void 작성자가_아니면_예외() {
            // given
            Project project = createDraftProject(1L, 100L);
            given(loadProjectPort.getById(1L)).willReturn(project);

            var command = SubmitProjectCommand.builder()
                .projectId(1L)
                .requesterMemberId(999L)
                .build();

            // when & then
            assertThatThrownBy(() -> sut.submit(command))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        }
    }

    // --- helpers ---

    private Project createDraftProject(Long projectId, Long ownerMemberId) {
        Project project = Project.createDraft(1L, 2L, ownerMemberId);
        ReflectionTestUtils.setField(project, "id", projectId);
        return project;
    }

    private ChallengerInfo challengerInfo(ChallengerPart part) {
        return ChallengerInfo.builder()
            .challengerId(1L)
            .memberId(100L)
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
}
