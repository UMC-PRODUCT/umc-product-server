package com.umc.product.project.application.service.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationDetailInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectApplicationSummaryInfo;
import com.umc.product.project.application.port.in.query.dto.SearchProjectApplicationsQuery;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPolicyPort;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectApplicationFormPolicy;
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

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectApplicationQueryService
    implements GetMyProjectApplicationsUseCase,
    SearchProjectApplicationsUseCase,
    GetProjectApplicationDetailUseCase {

    private final LoadProjectApplicationPort loadProjectApplicationPort;
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
     * 권한 검사(4종 staff + 본인 / DRAFT 본인 한정)는 컨트롤러의 {@code @CheckAccess(PROJECT_APPLICATION, READ)} 가 처리하므로 본 메서드 진입 시점에는
     * L2 권한이 검증된 상태이다.
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

    /**
     * 본인 지원 내역 목록 조회.
     * <p>
     * 사용자의 챌린저 파트로 {@link MatchingType} 을 자동 결정하여 필터링하고, 매칭 라운드 시작일 ASC -> 지원서 갱신일 DESC 순으로 정렬된 ProjectApplication 목록을
     * 표준 view 로 반환한다.
     * <p>
     * 본 UseCase 는 자기 자원(ProjectApplication) 만 다룬다. 랜덤 매칭/운영진 강제 배정으로 합류한 {@code ProjectMember(application = null)} 는 별도
     * UseCase ({@link com.umc.product.project.application.port.in.query.GetRandomMatchedProjectMemberUseCase}) 로
     * 조회하며, 두 데이터원을 한 화면 카드 줄로 합성하는 책임은 Web Assembler 가 진다.
     * <p>
     * 사용자가 해당 기수의 챌린저가 아니거나 매칭 대상 파트가 아닌 경우(PLAN/ADMIN) 빈 리스트를 반환한다.
     */
    @Override
    public List<ProjectApplicationSummaryInfo> listMyApplications(GetMyProjectApplicationsQuery query) {
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
        return applications.stream()
            .map(ProjectApplicationSummaryInfo::from)
            .toList();
    }

    // ==============================================================
    //                      Helper Method
    // ==============================================================

    /**
     * PM/운영진용 단일 프로젝트 지원자 목록 조회.
     * <p>
     * 본 메서드는 자기 자원(ProjectApplication) 만 반환한다. 화면 카드에 들어가는 부가 정보 (지원자의 파트 / 매칭 라운드 / 닉네임 등) 는
     * Web Assembler 가 다른 도메인에서 합쳐 붙인다. 그래서 파트(part) 필터도 챌린저 도메인이 필요한 정보라 Assembler 단계에서 적용한다.
     * <p>
     * 권한이 없으면 (None scope) 존재 자체를 숨기려고 빈 리스트로 위장한다. 정렬은 repository 가 phase ASC → submittedAt ASC 로 보장한다.
     */
    @Override
    public List<ProjectApplicationSummaryInfo> searchByProject(SearchProjectApplicationsQuery query) {
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
        return applications.stream()
            .map(ProjectApplicationSummaryInfo::from)
            .toList();
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
            .flatMap(MatchingType::fromPart);
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
}
