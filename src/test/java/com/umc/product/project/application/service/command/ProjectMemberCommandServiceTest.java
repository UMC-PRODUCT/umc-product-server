package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.command.dto.AddProjectMemberCommand;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectMemberPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMember;
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
