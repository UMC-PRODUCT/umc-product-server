package com.umc.product.project.application.service.query;

import com.umc.product.common.domain.enums.ChallengerPart;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public Page<ProjectInfo> search(SearchProjectQuery query) {
        // TODO: N+1 — 페이지당 (3 × size + 1) 쿼리. batch 메서드 추가 필요:
        //  LoadProjectMemberPort.listByProjectIdsAndPart(Set<Long>, ChallengerPart)
        //  LoadProjectMemberPort.countByProjectIdsGroupByPart(Set<Long>)
        //  LoadProjectPartQuotaPort.listByProjectIds(Set<Long>)
        return loadProjectPort.search(query)
            .map(this::toProjectInfo);
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
