package com.umc.product.project.application.service.query;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.GetMyProjectApplicationsUseCase;
import com.umc.product.project.application.port.in.query.SearchProjectApplicationsUseCase;
import com.umc.product.project.application.port.in.query.dto.GetMyProjectApplicationsQuery;
import com.umc.product.project.application.port.in.query.dto.MyProjectApplicationCardInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationCardInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectPartQuotaInfo;
import com.umc.product.project.application.port.in.query.dto.SearchProjectApplicationsQuery;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectApplicationQueryService
    implements GetMyProjectApplicationsUseCase, SearchProjectApplicationsUseCase {

    private final LoadProjectApplicationPort loadProjectApplicationPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final LoadProjectPort loadProjectPort;

    // Cross-domain
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetFileUseCase getFileUseCase;

    private static Optional<MatchingType> matchingTypeOf(ChallengerPart part) {
        return switch (part) {
            case DESIGN -> Optional.of(MatchingType.PLAN_DESIGN);
            case WEB, ANDROID, IOS, NODEJS, SPRINGBOOT -> Optional.of(MatchingType.PLAN_DEVELOPER);
            case PLAN, ADMIN -> Optional.empty();
        };
    }

    /**
     * 본인 지원 내역 목록 조회 서비스.
     * <p>
     * 사용자의 파트로부터 {@link MatchingType} 을 자동 결정하여 필터링하고, 매칭 라운드 시작일 ASC → 지원서 갱신일 DESC 순으로 정렬된 카드 목록을 반환한다.
     * <p>
     * 사용자가 해당 기수의 챌린저가 아니거나 매칭 대상 파트가 아닌 경우(PLAN/ADMIN) 빈 리스트를 반환한다. 운영진 권한은 별도 {@code ChallengerRole} 로 표현되므로 본 결정에
     * 영향을 주지 않는다 — 운영진이면서 개발 파트 챌린저인 사용자도 본인 파트({@code WEB} 등) 기준으로 정상 조회된다.
     */
    @Override
    public List<MyProjectApplicationCardInfo> getMyApplications(GetMyProjectApplicationsQuery query) {
        Optional<MatchingType> matchingType = resolveMatchingType(query);
        if (matchingType.isEmpty()) {
            return List.of();
        }

        List<ProjectApplication> applications = loadProjectApplicationPort.searchMyApplications(
            query.requesterMemberId(),
            query.gisuId(),
            matchingType.get(),
            query.status()
        );
        if (applications.isEmpty()) {
            return List.of();
        }

        return assembleCards(applications);
    }

    // ==============================================================
    //                      Helper Method
    // ==============================================================

    /**
     * PM/운영진용 단일 프로젝트 지원자 목록 조회.
     * <p>
     * 흐름:
     * <ol>
     *   <li>프로젝트 단건 조회 (없으면 PROJECT_NOT_FOUND)</li>
     *   <li>지원서 동적 검색 (matchingRoundId / status 필터, PENDING 제외)</li>
     *   <li>지원자들의 challenger.part batch 조회 (해당 기수 invariant)</li>
     *   <li>part 필터를 in-memory 로 적용 -- challenger.part 는 ProjectApplication 컬럼이 아니므로
     *       repository 단에서 다루지 않는다 (도메인 분리)</li>
     *   <li>ProjectApplicationCardInfo 로 조립</li>
     * </ol>
     * <p>
     * 정렬은 repository 가 phase ASC -> submittedAt ASC 로 처리하므로 이 메서드는 추가 정렬을 하지 않는다.
     */
    @Override
    public List<ProjectApplicationCardInfo> searchByProject(SearchProjectApplicationsQuery query) {
        Project project = loadProjectPort.getById(query.projectId());

        List<ProjectApplication> applications = loadProjectApplicationPort.searchProjectApplications(
            query.projectId(),
            query.matchingRoundId(),
            query.status()
        );
        if (applications.isEmpty()) {
            return List.of();
        }

        Map<Long, ChallengerPart> partsByMember = resolveApplicantParts(applications, project.getGisuId());

        List<ProjectApplicationCardInfo> cards = new ArrayList<>(applications.size());
        for (ProjectApplication application : applications) {
            ChallengerPart applicantPart = partsByMember.get(application.getApplicantMemberId());
            if (query.part() != null && query.part() != applicantPart) {
                continue;
            }
            cards.add(ProjectApplicationCardInfo.of(application, applicantPart));
        }
        return cards;
    }

    /**
     * 요청자의 챌린저 파트로부터 매칭 종류를 결정한다.
     * <ul>
     *   <li>해당 기수에 챌린저 레코드 없음 -> empty</li>
     *   <li>{@code PLAN} / {@code ADMIN} -> empty (지원 대상 아님)</li>
     *   <li>{@code DESIGN} -> {@code PLAN_DESIGN}</li>
     *   <li>{@code WEB} / {@code ANDROID} / {@code IOS} / {@code NODEJS} / {@code SPRINGBOOT} -> {@code PLAN_DEVELOPER}</li>
     * </ul>
     */
    private Optional<MatchingType> resolveMatchingType(GetMyProjectApplicationsQuery query) {
        return getChallengerUseCase
            .findByMemberIdAndGisuId(query.requesterMemberId(), query.gisuId())
            .map(ChallengerInfo::part)
            .flatMap(ProjectApplicationQueryService::matchingTypeOf);
    }

    /**
     * 지원서 목록을 카드 Info 로 조립한다. partQuota / 파트별 멤버 수 / 썸네일 URL 은 배치로 일괄 조회한다.
     */
    private List<MyProjectApplicationCardInfo> assembleCards(List<ProjectApplication> applications) {
        Set<Long> projectIds = applications.stream()
            .map(a -> a.getApplicationForm().getProject().getId())
            .collect(Collectors.toSet());

        Map<Long, List<ProjectPartQuota>> quotasByProject =
            loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(projectIds);

        Map<Long, Map<ChallengerPart, Long>> countsByProject =
            loadProjectMemberPort.countByProjectIdsGroupByProjectIdAndPart(projectIds);

        Map<String, String> thumbnailLinks = resolveThumbnailLinks(applications);

        List<MyProjectApplicationCardInfo> cards = new ArrayList<>(applications.size());
        for (ProjectApplication application : applications) {
            Project project = application.getApplicationForm().getProject();
            List<ProjectPartQuotaInfo> partQuotaInfos = buildPartQuotaInfos(
                quotasByProject.getOrDefault(project.getId(), List.of()),
                countsByProject.getOrDefault(project.getId(), Map.of())
            );
            String thumbnailUrl = project.getThumbnailFileId() == null
                ? null
                : thumbnailLinks.get(project.getThumbnailFileId());

            cards.add(MyProjectApplicationCardInfo.of(application, partQuotaInfos, thumbnailUrl));
        }
        return cards;
    }

    private List<ProjectPartQuotaInfo> buildPartQuotaInfos(
        List<ProjectPartQuota> quotas,
        Map<ChallengerPart, Long> currentCounts
    ) {
        if (quotas.isEmpty()) {
            return List.of();
        }
        return quotas.stream()
            .map(q -> ProjectPartQuotaInfo.of(
                q.getPart(),
                q.getQuota(),
                currentCounts.getOrDefault(q.getPart(), 0L)
            ))
            .toList();
    }

    /**
     * 지원자 memberId 집합을 모아 challenger 도메인에서 part 를 batch 조회한다. 모든 지원자는 해당 기수의 챌린저여야 하며 (도메인 invariant), 누락 시 challenger
     * 도메인 측에서 예외를 던진다.
     */
    private Map<Long, ChallengerPart> resolveApplicantParts(
        List<ProjectApplication> applications, Long gisuId
    ) {
        Set<Long> memberIds = applications.stream()
            .map(ProjectApplication::getApplicantMemberId)
            .collect(Collectors.toSet());

        return getChallengerUseCase.batchGetByMemberIdsAndGisuId(memberIds, gisuId)
            .entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().part()));
    }

    private Map<String, String> resolveThumbnailLinks(List<ProjectApplication> applications) {
        Set<String> fileIds = new HashSet<>();
        for (ProjectApplication application : applications) {
            String fileId = application.getApplicationForm().getProject().getThumbnailFileId();
            if (fileId != null) {
                fileIds.add(fileId);
            }
        }
        if (fileIds.isEmpty()) {
            return Map.of();
        }
        return getFileUseCase.getFileLinks(new ArrayList<>(fileIds));
    }
}
