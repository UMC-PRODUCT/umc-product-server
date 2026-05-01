package com.umc.product.project.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.access.ProjectAccessScope;
import com.umc.product.project.application.access.ProjectAccessScopeResolver;
import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectQueryServiceTest {

    @Mock
    LoadProjectPort loadProjectPort;
    @Mock
    LoadProjectMemberPort loadProjectMemberPort;
    @Mock
    LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    @Mock
    GetFileUseCase getFileUseCase;
    @Mock
    ProjectAccessScopeResolver scopeResolver;

    @InjectMocks
    ProjectQueryService sut;

    @Test
    void getById_프로젝트_상세_조회_성공() {
        // given
        Project project = createProject(1L, ProjectStatus.IN_PROGRESS);
        given(loadProjectPort.getById(1L)).willReturn(project);
        given(loadProjectMemberPort.listByProjectIdAndPart(1L, ChallengerPart.PLAN))
            .willReturn(List.of());
        given(loadProjectPartQuotaPort.listByProjectId(1L)).willReturn(List.of());
        given(getFileUseCase.getFileLinks(List.of("thumb-1")))
            .willReturn(Map.of("thumb-1", "https://cdn.example.com/thumb-1"));

        // when
        ProjectInfo result = sut.getById(1L);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("테스트 프로젝트");
        assertThat(result.thumbnailImageUrl()).isEqualTo("https://cdn.example.com/thumb-1");
        assertThat(result.coProductOwnerMemberIds()).isEmpty();
    }

    @Test
    void getById_존재하지_않으면_예외() {
        // given
        given(loadProjectPort.getById(999L))
            .willThrow(new ProjectDomainException(
                com.umc.product.project.domain.exception.ProjectErrorCode.PROJECT_NOT_FOUND));

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> sut.getById(999L))
            .isInstanceOf(ProjectDomainException.class);
    }

    @Test
    void getById_coPM과_partQuota_포함_조회() {
        // given
        Project project = createProject(1L, ProjectStatus.IN_PROGRESS);
        ReflectionTestUtils.setField(project, "productOwnerMemberId", 10L);

        given(loadProjectPort.getById(1L)).willReturn(project);

        var coPm = createProjectMember(20L);
        given(loadProjectMemberPort.listByProjectIdAndPart(1L, ChallengerPart.PLAN))
            .willReturn(List.of(coPm));

        var quota = createPartQuota(1L, ChallengerPart.WEB, 3);
        given(loadProjectPartQuotaPort.listByProjectId(1L)).willReturn(List.of(quota));
        given(loadProjectMemberPort.countByProjectIdGroupByPart(1L))
            .willReturn(Map.of(ChallengerPart.WEB, 2L));
        given(getFileUseCase.getFileLinks(List.of("thumb-1")))
            .willReturn(Map.of("thumb-1", "https://cdn.example.com/thumb-1"));

        // when
        ProjectInfo result = sut.getById(1L);

        // then
        assertThat(result.coProductOwnerMemberIds()).containsExactly(20L);
        assertThat(result.partQuotas()).hasSize(1);
        assertThat(result.partQuotas().get(0).part()).isEqualTo(ChallengerPart.WEB);
        assertThat(result.partQuotas().get(0).quota()).isEqualTo(3);
        assertThat(result.partQuotas().get(0).currentCount()).isEqualTo(2);
    }

    @Test
    void findDraftByOwnerAndGisu_DRAFT_프로젝트_반환() {
        // given
        Project project = createProject(1L, ProjectStatus.DRAFT);
        given(loadProjectPort.findByOwnerAndGisu(10L, 1L))
            .willReturn(Optional.of(project));
        given(loadProjectMemberPort.listByProjectIdAndPart(1L, ChallengerPart.PLAN))
            .willReturn(List.of());
        given(loadProjectPartQuotaPort.listByProjectId(1L)).willReturn(List.of());
        given(getFileUseCase.getFileLinks(List.of("thumb-1")))
            .willReturn(Map.of("thumb-1", "https://cdn.example.com/thumb-1"));

        // when
        Optional<ProjectInfo> result = sut.findDraftByOwnerAndGisu(10L, 1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().status()).isEqualTo(ProjectStatus.DRAFT);
    }

    @Test
    void findDraftByOwnerAndGisu_DRAFT가_아니면_empty() {
        // given
        Project project = createProject(1L, ProjectStatus.IN_PROGRESS);
        given(loadProjectPort.findByOwnerAndGisu(10L, 1L))
            .willReturn(Optional.of(project));

        // when
        Optional<ProjectInfo> result = sut.findDraftByOwnerAndGisu(10L, 1L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findDraftByOwnerAndGisu_프로젝트_없으면_empty() {
        // given
        given(loadProjectPort.findByOwnerAndGisu(10L, 1L))
            .willReturn(Optional.empty());

        // when
        Optional<ProjectInfo> result = sut.findDraftByOwnerAndGisu(10L, 1L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void search_페이지_결과_변환() {
        // given
        Project project = createProject(1L, ProjectStatus.IN_PROGRESS);
        PageRequest pageable = PageRequest.of(0, 20);
        SearchProjectQuery query = SearchProjectQuery.forChallenger(
            1L, null, null, null, null, null, pageable);

        given(scopeResolver.resolveForPublicSearch(any(), any(), anySet()))
            .willReturn(new ProjectAccessScope.PublicOnly());
        given(loadProjectPort.search(any(SearchProjectQuery.class)))
            .willReturn(new PageImpl<>(List.of(project), pageable, 1));
        given(loadProjectMemberPort.listByProjectIdAndPart(1L, ChallengerPart.PLAN))
            .willReturn(List.of());
        given(loadProjectPartQuotaPort.listByProjectId(1L)).willReturn(List.of());
        given(getFileUseCase.getFileLinks(List.of("thumb-1")))
            .willReturn(Map.of("thumb-1", "https://cdn.example.com/thumb-1"));

        // when
        Page<ProjectInfo> result = sut.search(query, 99L);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
    }

    @Test
    void search_빈_결과() {
        // given
        PageRequest pageable = PageRequest.of(0, 20);
        SearchProjectQuery query = SearchProjectQuery.forChallenger(
            1L, null, null, null, null, null, pageable);

        given(scopeResolver.resolveForPublicSearch(any(), any(), anySet()))
            .willReturn(new ProjectAccessScope.PublicOnly());
        given(loadProjectPort.search(any(SearchProjectQuery.class)))
            .willReturn(new PageImpl<>(List.of(), pageable, 0));

        // when
        Page<ProjectInfo> result = sut.search(query, 99L);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    // ========== Helper Methods ==========

    private Project createProject(Long id, ProjectStatus status) {
        Project project;
        try {
            var constructor = Project.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            project = constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ReflectionTestUtils.setField(project, "id", id);
        ReflectionTestUtils.setField(project, "gisuId", 1L);
        ReflectionTestUtils.setField(project, "chapterId", 1L);
        ReflectionTestUtils.setField(project, "status", status);
        ReflectionTestUtils.setField(project, "name", "테스트 프로젝트");
        ReflectionTestUtils.setField(project, "description", "테스트 설명");
        ReflectionTestUtils.setField(project, "thumbnailFileId", "thumb-1");
        ReflectionTestUtils.setField(project, "productOwnerMemberId", 10L);
        return project;
    }

    private com.umc.product.project.domain.ProjectMember createProjectMember(Long memberId) {
        com.umc.product.project.domain.ProjectMember pm;
        try {
            var constructor = com.umc.product.project.domain.ProjectMember.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            pm = constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ReflectionTestUtils.setField(pm, "memberId", memberId);
        ReflectionTestUtils.setField(pm, "part", ChallengerPart.PLAN);
        return pm;
    }

    private ProjectPartQuota createPartQuota(Long projectId, ChallengerPart part, int quota) {
        ProjectPartQuota pq;
        try {
            var constructor = ProjectPartQuota.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            pq = constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ReflectionTestUtils.setField(pq, "part", part);
        ReflectionTestUtils.setField(pq, "quota", (long) quota);
        return pq;
    }
}
