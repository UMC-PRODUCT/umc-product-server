package com.umc.product.project.application.service.query;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.access.ProjectApplicationAccessScope;
import com.umc.product.project.application.access.ProjectApplicationAccessScopeResolver;
import com.umc.product.project.application.port.in.query.GetMyProjectApplicationsUseCase;
import com.umc.product.project.application.port.in.query.GetProjectApplicationDetailUseCase;
import com.umc.product.project.application.port.in.query.SearchProjectApplicationsUseCase;
import com.umc.product.project.application.port.in.query.dto.GetMyProjectApplicationsQuery;
import com.umc.product.project.application.port.in.query.dto.GetProjectApplicationDetailQuery;
import com.umc.product.project.application.port.in.query.dto.MyProjectApplicationCardInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationCardInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationDetailInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectPartQuotaInfo;
import com.umc.product.project.application.port.in.query.dto.SearchProjectApplicationsQuery;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPolicyPort;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectApplicationFormPolicy;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileInfo;
import com.umc.product.survey.application.port.in.query.GetFormResponseUseCase;
import com.umc.product.survey.application.port.in.query.GetFormUseCase;
import com.umc.product.survey.application.port.in.query.dto.AnswerInfo;
import com.umc.product.survey.application.port.in.query.dto.FormResponseWithAnswersInfo;
import com.umc.product.survey.application.port.in.query.dto.FormWithStructureInfo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    implements GetMyProjectApplicationsUseCase,
    SearchProjectApplicationsUseCase,
    GetProjectApplicationDetailUseCase {

    private final LoadProjectApplicationPort loadProjectApplicationPort;
    private final LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    private final LoadProjectMemberPort loadProjectMemberPort;
    private final LoadProjectPort loadProjectPort;
    private final LoadProjectApplicationFormPolicyPort loadProjectApplicationFormPolicyPort;
    private final ProjectApplicationAccessScopeResolver accessScopeResolver;

    // Cross-domain
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetFileUseCase getFileUseCase;
    private final GetFormUseCase getFormUseCase;
    private final GetFormResponseUseCase getFormResponseUseCase;

    /**
     * 지원서 단건 상세 조회.
     * <p>
     * 권한 검사(4종 staff + 본인 / DRAFT 본인 한정)는 컨트롤러의 {@code @CheckAccess(PROJECT_APPLICATION, READ)} 가
     * 처리하므로 본 메서드 진입 시점에는 L2 권한이 검증된 상태이다.
     * <p>
     * 흐름:
     * <ol>
     *   <li>application fetch join 단건 로드 (form/project/matchingRound 한 번에)</li>
     *   <li>정합성 검증: application 의 form.project.id 가 path 의 projectId 와 일치해야 함.
     *       위반/미존재 모두 PROJECT_APPLICATION_NOT_FOUND 로 통일하여 다른 프로젝트의 지원서 존재 여부를 은닉</li>
     *   <li>지원자 파트 단건 조회 (해당 기수 챌린저 invariant -- 누락 시 not-found 로 통일)</li>
     *   <li>폼 구조/정책/응답 본문/첨부 파일 raw 데이터 cross-domain 조회 (storage 만 IN 쿼리 batch)</li>
     *   <li>{@link ProjectApplicationDetailInfo#of} 가 마스킹/메타 분리/answers Map 합성까지 한 번에 처리</li>
     * </ol>
     */
    @Override
    public ProjectApplicationDetailInfo getDetail(GetProjectApplicationDetailQuery query) {
        ProjectApplication application = loadProjectApplicationPort.findByIdWithDetails(query.applicationId())
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_NOT_FOUND));

        ProjectApplicationForm applicationForm = application.getApplicationForm();
        Project project = applicationForm.getProject();
        if (!Objects.equals(project.getId(), query.projectId())) {
            throw new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_NOT_FOUND);
        }

        ChallengerPart applicantPart = getChallengerUseCase
            .findByMemberIdAndGisuId(application.getApplicantMemberId(), project.getGisuId())
            .map(ChallengerInfo::part)
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_NOT_FOUND));

        FormWithStructureInfo formStructure = getFormUseCase.getFormWithStructure(applicationForm.getFormId());
        List<ProjectApplicationFormPolicy> formPolicies =
            loadProjectApplicationFormPolicyPort.listByApplicationFormId(applicationForm.getId());
        // dangling formResponseId 는 invariant 위반이지만, 클라이언트에는 PROJECT_APPLICATION_NOT_FOUND 로 통일하여 다른 도메인 에러 leakage 를 차단한다.
        FormResponseWithAnswersInfo formResponseWithAnswers =
            getFormResponseUseCase.findResponseWithAnswers(application.getFormResponseId())
                .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_APPLICATION_NOT_FOUND));
        Map<String, FileInfo> filesByFileId = resolveFiles(formResponseWithAnswers.answers());

        return ProjectApplicationDetailInfo.of(
            application,
            applicantPart,
            formStructure,
            formPolicies,
            formResponseWithAnswers,
            filesByFileId
        );
    }

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
     * 사용자의 파트로부터 {@link MatchingType} 을 자동 결정하여 필터링하고, 매칭 라운드 시작일 ASC -> 지원서 갱신일 DESC 순으로 정렬된 application 카드와 끝에 append
     * 된 랜덤 매칭 카드를 반환한다.
     * <p>
     * 데이터 원천:
     * <ul>
     *   <li>application 카드 -- 본인이 제출한 {@code ProjectApplication} (DRAFT/SUBMITTED/APPROVED/REJECTED)</li>
     *   <li>랜덤 매칭 카드 -- 본인이 ACTIVE 멤버이면서 {@code application=null} 인 케이스 (자동 랜덤 매칭 / 운영진 강제 배정).
     *       도메인 정책상 한 챌린저는 한 기수에 한 프로젝트에만 합류 가능하므로 0 또는 1 건이며, status 는 ProjectMember 의 ACTIVE 가 곧 합격 의미이므로 APPROVED 로
     *       표시한다. 정상 합격(application APPROVED + ProjectMember 양쪽 존재) 케이스의 중복 노출은 application=null 필터로 자연스럽게 차단된다.</li>
     * </ul>
     * <p>
     * 사용자가 해당 기수의 챌린저가 아니거나 매칭 대상 파트가 아닌 경우(PLAN/ADMIN) 빈 리스트를 반환한다. 운영진 권한은 별도 {@code ChallengerRole} 로 표현되므로 본 결정에
     * 영향을 주지 않는다 -- 운영진이면서 개발 파트 챌린저인 사용자도 본인 파트({@code WEB} 등) 기준으로 정상 조회된다.
     * <p>
     * 랜덤 매칭 카드는 status 필터({@code query.status})와 무관하게 노출 여부가 결정된다 -- application 도메인 외부의 데이터원이라 application status 필터의
     * 시맨틱이 적용되지 않으며, 클라이언트가 SUBMITTED/REJECTED/DRAFT 등 좁은 필터로 호출했을 때 RANDOM_MATCHING 카드까지 끼는 것을 막기 위해
     * {@code status} 가 명시된 호출에서는 랜덤 매칭 카드를 합성하지 않는다.
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

        Optional<ProjectMember> randomMatchingMember = resolveRandomMatchingMember(query, matchingType.get());

        if (applications.isEmpty() && randomMatchingMember.isEmpty()) {
            return List.of();
        }

        return assembleCards(applications, randomMatchingMember, matchingType.get());
    }

    // ==============================================================
    //                      Helper Method
    // ==============================================================

    /**
     * application 카드와 랜덤 매칭 카드의 projectId 를 통합한 집합을 만든다 (N+1 방지용 batch 키).
     */
    private static Set<Long> collectProjectIds(
        List<ProjectApplication> applications, Optional<ProjectMember> randomMatchingMember
    ) {
        Set<Long> projectIds = new HashSet<>();
        for (ProjectApplication application : applications) {
            projectIds.add(application.getApplicationForm().getProject().getId());
        }
        randomMatchingMember.ifPresent(member -> projectIds.add(member.getProject().getId()));
        return projectIds;
    }

    private static String thumbnailUrlOf(Project project, Map<String, String> thumbnailLinks) {
        return project.getThumbnailFileId() == null ? null : thumbnailLinks.get(project.getThumbnailFileId());
    }

    /**
     * PM/운영진용 단일 프로젝트 지원자 목록 조회.
     * <p>
     * 흐름:
     * <ol>
     *   <li>권한 scope 결정 (PO/Sub-PM/SUPER_ADMIN/CC/지부장/학교장만 통과, 그 외 빈 리스트로 위장)</li>
     *   <li>프로젝트 단건 조회 (없으면 PROJECT_NOT_FOUND)</li>
     *   <li>지원서 동적 검색 (matchingRoundId / status 필터, DRAFT 제외)</li>
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

        ProjectApplicationAccessScope scope = accessScopeResolver.resolveForProjectApplicantList(
            query.requesterMemberId(), project);
        if (scope instanceof ProjectApplicationAccessScope.None) {
            return List.of();
        }

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
     * application 카드 + 랜덤 매칭 카드를 조립한다. partQuota / 파트별 멤버 수 / 썸네일 URL 은 두 데이터원의 projectId 통합 집합으로 1회 batch 조회한다.
     * <p>
     * 정렬: application 카드는 port 가 반환한 순서를 그대로 유지(매칭 라운드 시작일 ASC -> 갱신일 DESC), 랜덤 매칭 카드는 끝에 1건 append.
     */
    private List<MyProjectApplicationCardInfo> assembleCards(
        List<ProjectApplication> applications,
        Optional<ProjectMember> randomMatchingMember,
        MatchingType matchingType
    ) {
        Set<Long> projectIds = collectProjectIds(applications, randomMatchingMember);

        Map<Long, List<ProjectPartQuota>> quotasByProject =
            loadProjectPartQuotaPort.listByProjectIdsGroupedByProjectId(projectIds);

        Map<Long, Map<ChallengerPart, Long>> countsByProject =
            loadProjectMemberPort.countByProjectIdsGroupByProjectIdAndPart(projectIds);

        Map<String, String> thumbnailLinks = resolveThumbnailLinks(applications, randomMatchingMember);

        List<MyProjectApplicationCardInfo> cards = new ArrayList<>(applications.size() + 1);
        for (ProjectApplication application : applications) {
            Project project = application.getApplicationForm().getProject();
            cards.add(MyProjectApplicationCardInfo.of(
                application,
                buildPartQuotaInfos(
                    quotasByProject.getOrDefault(project.getId(), List.of()),
                    countsByProject.getOrDefault(project.getId(), Map.of())
                ),
                thumbnailUrlOf(project, thumbnailLinks)
            ));
        }
        randomMatchingMember.ifPresent(member -> {
            Project project = member.getProject();
            cards.add(MyProjectApplicationCardInfo.ofRandomMatching(
                member,
                buildPartQuotaInfos(
                    quotasByProject.getOrDefault(project.getId(), List.of()),
                    countsByProject.getOrDefault(project.getId(), Map.of())
                ),
                thumbnailUrlOf(project, thumbnailLinks),
                matchingType
            ));
        });
        return cards;
    }

    /**
     * 본인 ACTIVE + application=null 케이스를 단건 조회한다. APPLY-004 의 랜덤 매칭/운영진 강제 배정 카드 합성에 사용된다.
     * <p>
     * status 필터가 명시된 호출에서는 application status 시맨틱이 적용되지 않는 데이터원이라 합성을 생략한다 -- 자세한 설명은 {@link #getMyApplications} 의
     * javadoc 참조.
     */
    private Optional<ProjectMember> resolveRandomMatchingMember(
        GetMyProjectApplicationsQuery query, MatchingType matchingType
    ) {
        if (query.status() != null) {
            return Optional.empty();
        }
        return loadProjectMemberPort.findActiveWithoutApplicationByMemberIdAndGisuIdAndMatchingType(
            query.requesterMemberId(), query.gisuId(), matchingType
        );
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

    /**
     * 단건 상세에 포함될 모든 답변의 fileIds 를 모아 storage 도메인에서 IN 쿼리 1회로 batch 조회한다. 첨부 파일이 하나도 없으면 빈 Map 을 반환한다.
     */
    private Map<String, FileInfo> resolveFiles(List<AnswerInfo> answers) {
        Set<String> fileIds = new HashSet<>();
        for (AnswerInfo answer : answers) {
            if (answer.fileIds() != null) {
                fileIds.addAll(answer.fileIds());
            }
        }
        if (fileIds.isEmpty()) {
            return Map.of();
        }
        return getFileUseCase.findAllByIds(new ArrayList<>(fileIds));
    }

    /**
     * application 카드와 랜덤 매칭 카드의 thumbnailFileId 를 통합 집합으로 모아 storage 도메인에 IN 쿼리 1회 batch 조회한다.
     */
    private Map<String, String> resolveThumbnailLinks(
        List<ProjectApplication> applications, Optional<ProjectMember> randomMatchingMember
    ) {
        Set<String> fileIds = new HashSet<>();
        for (ProjectApplication application : applications) {
            String fileId = application.getApplicationForm().getProject().getThumbnailFileId();
            if (fileId != null) {
                fileIds.add(fileId);
            }
        }
        randomMatchingMember.ifPresent(member -> {
            String fileId = member.getProject().getThumbnailFileId();
            if (fileId != null) {
                fileIds.add(fileId);
            }
        });
        if (fileIds.isEmpty()) {
            return Map.of();
        }
        return getFileUseCase.getFileLinks(new ArrayList<>(fileIds));
    }
}
