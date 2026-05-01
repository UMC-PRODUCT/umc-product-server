package com.umc.product.project.application.service.query;

import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.access.ProjectAccessScope;
import com.umc.product.project.application.access.ProjectAccessScope.All;
import com.umc.product.project.application.access.ProjectAccessScope.ChapterScoped;
import com.umc.product.project.application.access.ProjectAccessScope.None;
import com.umc.product.project.application.access.ProjectAccessScope.OwnerOnly;
import com.umc.product.project.application.access.ProjectAccessScope.PublicOnly;
import com.umc.product.project.application.access.ProjectAccessScope.SchoolScoped;
import com.umc.product.project.application.access.ProjectAccessScopeResolver;
import com.umc.product.project.application.port.in.query.GetProjectUseCase;
import com.umc.product.project.application.port.in.query.SearchProjectUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectPartQuotaInfo;
import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectQueryService implements
    GetProjectUseCase,
    SearchProjectUseCase {

    private final LoadProjectPort loadProjectPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    private final ProjectAccessScopeResolver scopeResolver;

    // Cross-domain
    private final GetFileUseCase getFileUseCase;

    @Override
    public ProjectInfo getById(Long projectId) {
        Project project = loadProjectPort.getById(projectId);
        return toProjectInfo(project);
    }

    @Override
    public Optional<ProjectInfo> findDraftByOwnerAndGisu(Long productOwnerMemberId, Long gisuId) {
        return loadProjectPort.findByOwnerAndGisu(productOwnerMemberId, gisuId)
            .filter(project -> project.getStatus() == ProjectStatus.DRAFT)
            .map(this::toProjectInfo);
    }

    @Override
    public Page<ProjectInfo> search(SearchProjectQuery query, SubjectAttributes subject) {
        // TODO: N+1 — 페이지당 (3 × size + 1) 쿼리. batch 메서드 추가 필요:
        //  LoadProjectMemberPort.listByProjectIdsAndPart(Set<Long>, ChallengerPart)
        //  LoadProjectMemberPort.countByProjectIdsGroupByPart(Set<Long>)
        //  LoadProjectPartQuotaPort.listByProjectIds(Set<Long>)
        Set<ProjectStatus> requestedStatuses = new HashSet<>(query.statuses());
        ProjectAccessScope scope = scopeResolver.resolveForPublicSearch(
            subject, query.gisuId(), requestedStatuses);
        return applyScope(scope, query)
            .map(this::toProjectInfo);
    }

    /**
     * 결정된 {@link ProjectAccessScope} 를 {@link SearchProjectQuery} 에 반영해 어댑터에 위임한다.
     * <p>
     * PR3a 시점엔 PROJECT-001(공개 검색)만 호출하므로 실제 도달 경로는 {@code All} / {@code PublicOnly}.
     * 나머지 case 는 PR3b 의 PROJECT-006(관리 화면)에서 활성화된다.
     */
    private Page<Project> applyScope(ProjectAccessScope scope, SearchProjectQuery query) {
        return switch (scope) {
            case All(Set<ProjectStatus> statuses) ->
                loadProjectPort.search(query.withStatuses(statuses));
            case ChapterScoped(Long chapterId, Set<ProjectStatus> statuses) ->
                loadProjectPort.search(query.withChapterFilter(chapterId, statuses));
            case SchoolScoped(Long schoolId, Set<ProjectStatus> statuses) ->
                loadProjectPort.search(query.withSchoolFilter(schoolId, statuses));
            case OwnerOnly(Long memberId, Set<ProjectStatus> statuses) ->
                loadProjectPort.search(query.withOwnerFilter(memberId, statuses));
            case PublicOnly() ->
                loadProjectPort.search(query.withStatuses(Set.of(ProjectStatus.IN_PROGRESS)));
            case None() ->
                new PageImpl<>(List.of(), query.pageable(), 0L);
        };
    }

    /**
     * Project 엔티티를 ProjectInfo로 조립합니다.
     * coPM 목록, 파트별 TO, 파일 URL을 함께 조회합니다.
     */
    private ProjectInfo toProjectInfo(Project project) {
        List<Long> coPmMemberIds = extractCoPmMemberIds(project);
        List<ProjectPartQuotaInfo> partQuotas = buildPartQuotas(project.getId());
        Map<String, String> fileLinks = resolveFileLinks(project);

        return ProjectInfo.from(
            project,
            coPmMemberIds,
            partQuotas,
            resolveLink(fileLinks, project.getThumbnailFileId()),
            resolveLink(fileLinks, project.getLogoFileId())
        );
    }

    private String resolveLink(Map<String, String> fileLinks, String fileId) {
        return fileId == null ? null : fileLinks.get(fileId);
    }

    /**
     * PLAN 파트 멤버 중 메인 PO를 제외한 보조 PM Member ID 목록을 추출합니다.
     */
    private List<Long> extractCoPmMemberIds(Project project) {
        List<ProjectMember> planMembers = loadProjectMemberPort
            .listByProjectIdAndPart(project.getId(), ChallengerPart.PLAN);

        return planMembers.stream()
            .map(ProjectMember::getMemberId)
            .filter(memberId -> !memberId.equals(project.getProductOwnerMemberId()))
            .toList();
    }

    /**
     * 파트별 TO 정보를 조립합니다 (quota + 현재 활성 멤버 수).
     */
    private List<ProjectPartQuotaInfo> buildPartQuotas(Long projectId) {
        List<ProjectPartQuota> quotas = loadProjectPartQuotaPort.listByProjectId(projectId);
        if (quotas.isEmpty()) {
            return List.of();
        }

        Map<ChallengerPart, Long> currentCounts = loadProjectMemberPort
            .countByProjectIdGroupByPart(projectId);

        return quotas.stream()
            .map(q -> ProjectPartQuotaInfo.of(
                q.getPart(),
                q.getQuota(),
                currentCounts.getOrDefault(q.getPart(), 0L)
            ))
            .toList();
    }

    /**
     * 썸네일/로고 파일 ID → CDN URL을 일괄 조회합니다.
     */
    private Map<String, String> resolveFileLinks(Project project) {
        List<String> fileIds = new ArrayList<>();
        if (project.getThumbnailFileId() != null) {
            fileIds.add(project.getThumbnailFileId());
        }
        if (project.getLogoFileId() != null) {
            fileIds.add(project.getLogoFileId());
        }

        if (fileIds.isEmpty()) {
            return Map.of();
        }

        return getFileUseCase.getFileLinks(fileIds);
    }
}
