package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.form.application.port.in.command.ManageFormUseCase;
import com.umc.product.project.application.port.in.command.dto.DeleteProjectCommand;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationFormPolicyPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationFormPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationPort;
import com.umc.product.project.application.port.out.SaveProjectMemberPort;
import com.umc.product.project.application.port.out.SaveProjectPartQuotaPort;
import com.umc.product.project.application.port.out.SaveProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

@ExtendWith(MockitoExtension.class)
class ProjectDeletionCommandServiceTest {

    @Mock LoadProjectPort loadProjectPort;
    @Mock LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    @Mock LoadProjectApplicationFormPort loadProjectApplicationFormPort;
    @Mock SaveProjectApplicationPort saveProjectApplicationPort;
    @Mock SaveProjectMemberPort saveProjectMemberPort;
    @Mock SaveProjectPartQuotaPort saveProjectPartQuotaPort;
    @Mock SaveProjectApplicationFormPolicyPort saveProjectApplicationFormPolicyPort;
    @Mock SaveProjectApplicationFormPort saveProjectApplicationFormPort;
    @Mock SaveProjectPort saveProjectPort;
    @Mock ManageFormUseCase manageFormUseCase;
    @Mock CheckPermissionUseCase checkPermissionUseCase;
    @InjectMocks ProjectDeletionCommandService sut;

    @ParameterizedTest
    @EnumSource(ProjectStatus.class)
    void 지원기간_밖에서는_상태와_무관하게_삭제한다(ProjectStatus status) {
        // given
        Project project = givenProject(status);

        // when
        sut.delete(command());

        // then
        verify(saveProjectMemberPort).deleteAllByProjectId(1L);
        verify(saveProjectApplicationPort).deleteAllByProjectId(1L);
        verify(saveProjectPort).delete(project);
    }

    @ParameterizedTest
    @EnumSource(value = MatchingPhase.class, names = {"FIRST", "SECOND", "THIRD"})
    void 각_차수의_지원기간에는_삭제를_거부한다(MatchingPhase phase) {
        // given
        givenProject(ProjectStatus.IN_PROGRESS);
        ProjectMatchingRound round = round(10L, phase, -3600, 3600, 7200);
        given(loadProjectMatchingRoundPort.listByChapterIdForUpdate(2L)).willReturn(List.of(round));

        // when & then
        assertThatThrownBy(() -> sut.delete(command()))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode").isEqualTo(ProjectErrorCode.PROJECT_DELETE_DURING_APPLICATION_PERIOD);
        verify(saveProjectMemberPort, never()).deleteAllByProjectId(any());
        verify(saveProjectApplicationPort, never()).deleteAllByProjectId(any());
        verify(saveProjectPort, never()).delete(any());
    }

    @Test
    void 판정기간과_랜덤매칭은_삭제를_막지_않는다() {
        // given
        Project project = givenProject(ProjectStatus.IN_PROGRESS);
        ProjectMatchingRound decision = round(20L, MatchingPhase.FIRST, -7200, -3600, 3600);
        ProjectMatchingRound random = round(10L, MatchingPhase.RANDOM_MATCHING, -3600, 3600, 3600);
        given(loadProjectMatchingRoundPort.listByChapterIdForUpdate(2L)).willReturn(List.of(decision, random));

        // when
        sut.delete(command());

        // then — 차수를 프로젝트보다 먼저 잠근다.
        var order = inOrder(loadProjectMatchingRoundPort, loadProjectPort, saveProjectPort);
        order.verify(loadProjectMatchingRoundPort).listByChapterIdForUpdate(2L);
        order.verify(loadProjectPort).getByIdForUpdate(1L);
        order.verify(saveProjectPort).delete(project);
    }

    @Test
    void 모든_폼을_정리하고_지원서를_폼보다_먼저_삭제한다() {
        // given
        Project project = givenProject(ProjectStatus.IN_PROGRESS);
        ProjectApplicationForm first = ProjectApplicationForm.create(project, 100L);
        ProjectApplicationForm second = ProjectApplicationForm.create(project, 200L);
        ReflectionTestUtils.setField(first, "id", 10L);
        ReflectionTestUtils.setField(second, "id", 20L);
        given(loadProjectApplicationFormPort.listByProjectId(1L)).willReturn(List.of(first, second));

        // when
        sut.delete(command());

        // then
        var order = inOrder(saveProjectMemberPort, saveProjectApplicationPort,
            saveProjectApplicationFormPolicyPort, saveProjectApplicationFormPort,
            manageFormUseCase, saveProjectPartQuotaPort, saveProjectPort);
        order.verify(saveProjectMemberPort).deleteAllByProjectId(1L);
        order.verify(saveProjectApplicationPort).deleteAllByProjectId(1L);
        order.verify(saveProjectApplicationFormPolicyPort).deleteAllByApplicationFormId(10L);
        order.verify(saveProjectApplicationFormPolicyPort).deleteAllByApplicationFormId(20L);
        order.verify(saveProjectApplicationFormPort).deleteAllByProjectId(1L);
        order.verify(manageFormUseCase).deleteForm(argThat(c -> c.formId().equals(100L)));
        order.verify(manageFormUseCase).deleteForm(argThat(c -> c.formId().equals(200L)));
        order.verify(saveProjectPartQuotaPort).deleteAllByProjectId(1L);
        order.verify(saveProjectPort).delete(project);
    }

    private Project givenProject(ProjectStatus status) {
        Project project = Project.createDraft(1L, 2L, 99L, 3L, 99L);
        ReflectionTestUtils.setField(project, "id", 1L);
        ReflectionTestUtils.setField(project, "status", status);
        given(loadProjectPort.getById(1L)).willReturn(project);
        given(loadProjectPort.getByIdForUpdate(1L)).willReturn(project);
        return project;
    }

    private ProjectMatchingRound round(Long id, MatchingPhase phase, long start, long end, long deadline) {
        Instant now = Instant.now();
        ProjectMatchingRound round = ProjectMatchingRound.create("테스트", null, MatchingType.PLAN_DEVELOPER,
            phase, 2L, now.plusSeconds(start), now.plusSeconds(end), now.plusSeconds(deadline),
            phase == MatchingPhase.RANDOM_MATCHING ? now.plusSeconds(deadline + 3600) : null);
        ReflectionTestUtils.setField(round, "id", id);
        return round;
    }

    private DeleteProjectCommand command() {
        return DeleteProjectCommand.builder().projectId(1L).requesterMemberId(99L).build();
    }
}
