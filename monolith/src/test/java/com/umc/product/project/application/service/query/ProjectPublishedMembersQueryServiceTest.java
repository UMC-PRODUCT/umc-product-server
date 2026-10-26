package com.umc.product.project.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.service.evaluator.ProjectPermissionEvaluator;
import com.umc.product.project.application.service.evaluator.SuperAdminProperties;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.enums.ProjectStatus;

@ExtendWith(MockitoExtension.class)
class ProjectPublishedMembersQueryServiceTest {

    @Mock
    CheckPermissionUseCase checkPermissionUseCase;
    @Mock
    LoadProjectPort loadProjectPort;
    @Mock
    LoadProjectMemberPort loadProjectMemberPort;
    @Mock
    GetMemberUseCase getMemberUseCase;
    ProjectPublishedMembersQueryService sut;

    @BeforeEach
    void setUp() {
        ProjectPermissionEvaluator evaluator = new ProjectPermissionEvaluator(loadProjectPort, new SuperAdminProperties(false));
        sut = new ProjectPublishedMembersQueryService(checkPermissionUseCase, loadProjectPort, evaluator,
            loadProjectMemberPort, getMemberUseCase);
    }

    @Test
    void 공개_명단은_권한_없는_프로젝트만_제외하고_조회_가능한_프로젝트를_반환한다() {
        // given
        Project published = project(1L, ProjectStatus.IN_PROGRESS);
        Project draft = project(2L, ProjectStatus.DRAFT);
        List<Long> projectIds = List.of(1L, 2L, 3L);
        given(loadProjectPort.listByIds(projectIds)).willReturn(List.of(published, draft));
        given(checkPermissionUseCase.loadSubject(900L)).willReturn(SubjectAttributes.builder().memberId(900L).build());

        // when
        var result = sut.get(projectIds, 900L);

        // then
        assertThat(result).containsOnlyKeys(1L);
        then(loadProjectPort).should().listByIds(projectIds);
        then(loadProjectPort).shouldHaveNoMoreInteractions();
        then(checkPermissionUseCase).should().loadSubject(900L);
        then(checkPermissionUseCase).shouldHaveNoMoreInteractions();
    }

    private Project project(Long projectId, ProjectStatus status) {
        Project project = Project.createDraft(1L, 10L, 99L, 1L, 99L);
        ReflectionTestUtils.setField(project, "id", projectId);
        ReflectionTestUtils.setField(project, "status", status);
        return project;
    }
}
