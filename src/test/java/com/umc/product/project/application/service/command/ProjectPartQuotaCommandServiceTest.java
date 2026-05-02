package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand;
import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand.Entry;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectPartQuotaPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectPartQuotaCommandServiceTest {

    @Mock
    LoadProjectPort loadProjectPort;
    @Mock
    LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    @Mock
    SaveProjectPartQuotaPort saveProjectPartQuotaPort;

    @InjectMocks
    ProjectPartQuotaCommandService sut;

    @Test
    void update_신규_파트_추가() {
        Long projectId = 42L;
        Project project = project(projectId, ProjectStatus.PENDING_REVIEW);
        given(loadProjectPort.getById(projectId)).willReturn(project);
        given(loadProjectPartQuotaPort.listByProjectId(projectId)).willReturn(List.of());

        sut.update(UpdatePartQuotasCommand.builder()
            .projectId(projectId)
            .entries(List.of(
                Entry.builder().part(ChallengerPart.SPRINGBOOT).quota(3L).build(),
                Entry.builder().part(ChallengerPart.WEB).quota(2L).build()
            ))
            .requesterMemberId(99L)
            .build());

        ArgumentCaptor<List<ProjectPartQuota>> captor = ArgumentCaptor.forClass(List.class);
        verify(saveProjectPartQuotaPort).saveAll(captor.capture());
        verify(saveProjectPartQuotaPort, never()).deleteByProjectIdAndPartIn(any(), any());

        List<ProjectPartQuota> saved = captor.getValue();
        assertThat(saved).hasSize(2);
    }

    @Test
    void update_기존_파트_quota_변경() {
        Long projectId = 42L;
        Project project = project(projectId, ProjectStatus.PENDING_REVIEW);
        given(loadProjectPort.getById(projectId)).willReturn(project);

        ProjectPartQuota existing = ProjectPartQuota.create(project, ChallengerPart.SPRINGBOOT, 3L, 99L);
        ReflectionTestUtils.setField(existing, "id", 1L);
        given(loadProjectPartQuotaPort.listByProjectId(projectId)).willReturn(List.of(existing));

        sut.update(UpdatePartQuotasCommand.builder()
            .projectId(projectId)
            .entries(List.of(
                Entry.builder().part(ChallengerPart.SPRINGBOOT).quota(5L).build()
            ))
            .requesterMemberId(99L)
            .build());

        assertThat(existing.getQuota()).isEqualTo(5L);
        verify(saveProjectPartQuotaPort).saveAll(any());
    }

    @Test
    void update_본문에_없는_파트는_삭제() {
        Long projectId = 42L;
        Project project = project(projectId, ProjectStatus.PENDING_REVIEW);
        given(loadProjectPort.getById(projectId)).willReturn(project);

        ProjectPartQuota webQuota = ProjectPartQuota.create(project, ChallengerPart.WEB, 2L, 99L);
        ProjectPartQuota nodeQuota = ProjectPartQuota.create(project, ChallengerPart.NODEJS, 2L, 99L);
        given(loadProjectPartQuotaPort.listByProjectId(projectId))
            .willReturn(List.of(webQuota, nodeQuota));

        sut.update(UpdatePartQuotasCommand.builder()
            .projectId(projectId)
            .entries(List.of(Entry.builder().part(ChallengerPart.WEB).quota(3L).build()))
            .requesterMemberId(99L)
            .build());

        verify(saveProjectPartQuotaPort, times(1))
            .deleteByProjectIdAndPartIn(eq(projectId), eq(Set.of(ChallengerPart.NODEJS)));
    }

    @Test
    void update_iOS_DESIGN_조합은_금지() {
        Long projectId = 42L;
        Project project = project(projectId, ProjectStatus.PENDING_REVIEW);
        given(loadProjectPort.getById(projectId)).willReturn(project);

        assertThatThrownBy(() -> sut.update(UpdatePartQuotasCommand.builder()
                .projectId(projectId)
                .entries(List.of(
                    Entry.builder().part(ChallengerPart.IOS).quota(2L).build(),
                    Entry.builder().part(ChallengerPart.DESIGN).quota(2L).build()
                ))
                .requesterMemberId(99L)
                .build()))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_PART_COMBINATION_FORBIDDEN);
    }

    @Test
    void update_quota_0_이하면_거부() {
        Long projectId = 42L;
        Project project = project(projectId, ProjectStatus.PENDING_REVIEW);
        given(loadProjectPort.getById(projectId)).willReturn(project);

        assertThatThrownBy(() -> sut.update(UpdatePartQuotasCommand.builder()
                .projectId(projectId)
                .entries(List.of(Entry.builder().part(ChallengerPart.WEB).quota(0L).build()))
                .requesterMemberId(99L)
                .build()))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_PART_QUOTA_INVALID);
    }

    @Test
    void update_중복_파트_거부() {
        Long projectId = 42L;
        Project project = project(projectId, ProjectStatus.PENDING_REVIEW);
        given(loadProjectPort.getById(projectId)).willReturn(project);

        assertThatThrownBy(() -> sut.update(UpdatePartQuotasCommand.builder()
                .projectId(projectId)
                .entries(List.of(
                    Entry.builder().part(ChallengerPart.WEB).quota(2L).build(),
                    Entry.builder().part(ChallengerPart.WEB).quota(3L).build()
                ))
                .requesterMemberId(99L)
                .build()))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_PART_QUOTA_DUPLICATE);
    }

    @Test
    void update_COMPLETED_프로젝트는_거부() {
        Long projectId = 42L;
        Project project = project(projectId, ProjectStatus.COMPLETED);
        given(loadProjectPort.getById(projectId)).willReturn(project);

        assertThatThrownBy(() -> sut.update(UpdatePartQuotasCommand.builder()
                .projectId(projectId)
                .entries(List.of(Entry.builder().part(ChallengerPart.WEB).quota(2L).build()))
                .requesterMemberId(99L)
                .build()))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_INVALID_STATE);
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
            ReflectionTestUtils.setField(p, "productOwnerMemberId", 100L);
            ReflectionTestUtils.setField(p, "productOwnerSchoolId", 7L);
            return p;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
