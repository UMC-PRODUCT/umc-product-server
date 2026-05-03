package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.command.dto.AddProjectMemberCommand;
import com.umc.product.project.application.port.in.command.dto.RemoveProjectMemberCommand;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectMemberPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectMemberCommandServiceTest {

    @Mock
    LoadProjectPort loadProjectPort;
    @Mock
    LoadProjectMemberPort loadProjectMemberPort;
    @Mock
    SaveProjectMemberPort saveProjectMemberPort;

    @InjectMocks
    ProjectMemberCommandService sut;

    @Test
    void add_정상_추가_시_저장된_id_반환() {
        Long projectId = 42L;
        Project project = project(projectId, ProjectStatus.DRAFT);
        given(loadProjectPort.getById(projectId)).willReturn(project);
        given(loadProjectMemberPort.findByProjectIdAndMemberId(projectId, 200L))
            .willReturn(Optional.empty());
        given(saveProjectMemberPort.save(any(ProjectMember.class))).willAnswer(inv -> {
            ProjectMember pm = inv.getArgument(0);
            ReflectionTestUtils.setField(pm, "id", 999L);
            return pm;
        });

        AddProjectMemberCommand command = AddProjectMemberCommand.builder()
            .projectId(projectId).memberId(200L).part(ChallengerPart.PLAN).requesterMemberId(99L).build();

        Long result = sut.add(command);

        assertThat(result).isEqualTo(999L);
    }

    @Test
    void add_이미_존재하는_멤버면_예외() {
        Long projectId = 42L;
        Project project = project(projectId, ProjectStatus.DRAFT);
        given(loadProjectPort.getById(projectId)).willReturn(project);

        ProjectMember existing = projectMember(200L, ChallengerPart.PLAN);
        given(loadProjectMemberPort.findByProjectIdAndMemberId(projectId, 200L))
            .willReturn(Optional.of(existing));

        AddProjectMemberCommand command = AddProjectMemberCommand.builder()
            .projectId(projectId).memberId(200L).part(ChallengerPart.PLAN).requesterMemberId(99L).build();

        assertThatThrownBy(() -> sut.add(command))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode").isEqualTo(ProjectErrorCode.PROJECT_MEMBER_ALREADY_EXISTS);
    }

    @Test
    void add_COMPLETED_프로젝트에는_추가_불가() {
        Long projectId = 42L;
        Project project = project(projectId, ProjectStatus.COMPLETED);
        given(loadProjectPort.getById(projectId)).willReturn(project);

        AddProjectMemberCommand command = AddProjectMemberCommand.builder()
            .projectId(projectId).memberId(200L).part(ChallengerPart.PLAN).requesterMemberId(99L).build();

        assertThatThrownBy(() -> sut.add(command))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode").isEqualTo(ProjectErrorCode.PROJECT_INVALID_STATE);
    }

    @Test
    void add_ABORTED_프로젝트에는_추가_불가() {
        Long projectId = 42L;
        Project project = project(projectId, ProjectStatus.ABORTED);
        given(loadProjectPort.getById(projectId)).willReturn(project);

        AddProjectMemberCommand command = AddProjectMemberCommand.builder()
            .projectId(projectId).memberId(200L).part(ChallengerPart.PLAN).requesterMemberId(99L).build();

        assertThatThrownBy(() -> sut.add(command))
            .isInstanceOf(ProjectDomainException.class);
    }

    // --- remove ---

    @Test
    void remove_DRAFT_단계는_hardDelete_호출() {
        Long projectId = 42L;
        Project project = project(projectId, ProjectStatus.DRAFT);
        given(loadProjectPort.getById(projectId)).willReturn(project);

        ProjectMember member = projectMember(200L, ChallengerPart.PLAN);
        ReflectionTestUtils.setField(member, "id", 555L);
        given(loadProjectMemberPort.findByProjectIdAndMemberId(projectId, 200L))
            .willReturn(Optional.of(member));

        sut.remove(RemoveProjectMemberCommand.builder()
            .projectId(projectId).memberId(200L).reason("실수 추가").requesterMemberId(99L).build());

        verify(saveProjectMemberPort).hardDelete(555L);
        verify(saveProjectMemberPort, never()).save(any());
    }

    @Test
    void remove_PENDING_REVIEW_단계도_hardDelete() {
        Long projectId = 42L;
        Project project = project(projectId, ProjectStatus.PENDING_REVIEW);
        given(loadProjectPort.getById(projectId)).willReturn(project);

        ProjectMember member = projectMember(200L, ChallengerPart.PLAN);
        ReflectionTestUtils.setField(member, "id", 555L);
        given(loadProjectMemberPort.findByProjectIdAndMemberId(projectId, 200L))
            .willReturn(Optional.of(member));

        sut.remove(RemoveProjectMemberCommand.builder()
            .projectId(projectId).memberId(200L).reason(null).requesterMemberId(99L).build());

        verify(saveProjectMemberPort).hardDelete(555L);
    }

    @Test
    void remove_IN_PROGRESS_단계는_soft_delete_DISMISSED() {
        Long projectId = 42L;
        Project project = project(projectId, ProjectStatus.IN_PROGRESS);
        given(loadProjectPort.getById(projectId)).willReturn(project);

        ProjectMember member = projectMember(200L, ChallengerPart.PLAN);
        ReflectionTestUtils.setField(member, "id", 555L);
        given(loadProjectMemberPort.findByProjectIdAndMemberId(projectId, 200L))
            .willReturn(Optional.of(member));

        sut.remove(RemoveProjectMemberCommand.builder()
            .projectId(projectId).memberId(200L).reason("팀 적합도 부족").requesterMemberId(99L).build());

        verify(saveProjectMemberPort).save(member);
        verify(saveProjectMemberPort, never()).hardDelete(any());
        assertThat(member.getStatus()).isEqualTo(ProjectMemberStatus.DISMISSED);
        assertThat(member.getStatusChangeReason()).isEqualTo("팀 적합도 부족");
        assertThat(member.getStatusChangedMemberId()).isEqualTo(99L);
    }

    @Test
    void remove_메인_PM은_양도_API_안내_예외() {
        Long projectId = 42L;
        Project project = project(projectId, ProjectStatus.DRAFT);
        given(loadProjectPort.getById(projectId)).willReturn(project);

        // 메인 PM 의 memberId = 99L (project() helper 에서 설정)
        assertThatThrownBy(() -> sut.remove(RemoveProjectMemberCommand.builder()
                .projectId(projectId).memberId(99L).requesterMemberId(99L).build()))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode").isEqualTo(ProjectErrorCode.PROJECT_MAIN_PM_REMOVAL_REQUIRES_TRANSFER);
    }

    @Test
    void remove_없는_멤버면_예외() {
        Long projectId = 42L;
        Project project = project(projectId, ProjectStatus.DRAFT);
        given(loadProjectPort.getById(projectId)).willReturn(project);
        given(loadProjectMemberPort.findByProjectIdAndMemberId(projectId, 200L))
            .willReturn(Optional.empty());

        assertThatThrownBy(() -> sut.remove(RemoveProjectMemberCommand.builder()
                .projectId(projectId).memberId(200L).requesterMemberId(99L).build()))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode").isEqualTo(ProjectErrorCode.PROJECT_MEMBER_NOT_FOUND);
    }

    @Test
    void remove_COMPLETED_프로젝트는_거부() {
        Long projectId = 42L;
        Project project = project(projectId, ProjectStatus.COMPLETED);
        given(loadProjectPort.getById(projectId)).willReturn(project);

        ProjectMember member = projectMember(200L, ChallengerPart.PLAN);
        given(loadProjectMemberPort.findByProjectIdAndMemberId(projectId, 200L))
            .willReturn(Optional.of(member));

        assertThatThrownBy(() -> sut.remove(RemoveProjectMemberCommand.builder()
                .projectId(projectId).memberId(200L).requesterMemberId(99L).build()))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode").isEqualTo(ProjectErrorCode.PROJECT_INVALID_STATE);
    }

    // --- helpers ---

    private Project project(Long id, ProjectStatus status) {
        try {
            var c = Project.class.getDeclaredConstructor();
            c.setAccessible(true);
            Project p = c.newInstance();
            ReflectionTestUtils.setField(p, "id", id);
            ReflectionTestUtils.setField(p, "status", status);
            ReflectionTestUtils.setField(p, "gisuId", 1L);
            ReflectionTestUtils.setField(p, "chapterId", 1L);
            ReflectionTestUtils.setField(p, "productOwnerMemberId", 99L);
            ReflectionTestUtils.setField(p, "createdByMemberId", 99L);
            return p;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ProjectMember projectMember(Long memberId, ChallengerPart part) {
        try {
            var c = ProjectMember.class.getDeclaredConstructor();
            c.setAccessible(true);
            ProjectMember pm = c.newInstance();
            ReflectionTestUtils.setField(pm, "memberId", memberId);
            ReflectionTestUtils.setField(pm, "part", part);
            return pm;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
