package com.umc.product.recruitment.application.service.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberErrorCode;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.recruitment.adapter.in.web.dto.request.EvaluationDecision;
import com.umc.product.recruitment.adapter.in.web.mapper.AnswerInfoMapper;
import com.umc.product.recruitment.adapter.in.web.mapper.ApplicationDetailMapper;
import com.umc.product.recruitment.adapter.out.dto.ApplicationListItemProjection;
import com.umc.product.recruitment.adapter.out.dto.DocumentSelectionListItemProjection;
import com.umc.product.recruitment.adapter.out.dto.EvaluationListItemProjection;
import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.in.SortOption;
import com.umc.product.recruitment.application.port.in.query.GetApplicationDetailUseCase;
import com.umc.product.recruitment.application.port.in.query.GetApplicationEvaluationListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetApplicationListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetDocumentEvaluationRecruitmentListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetDocumentSelectionListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetMyDocumentEvaluationUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationDetailInfo;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationEvaluationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.DocumentEvaluationRecruitmentListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.DocumentEvaluationRecruitmentListInfo.DocumentEvaluationRecruitmentInfo;
import com.umc.product.recruitment.application.port.in.query.dto.DocumentSelectionApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationDetailQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationEvaluationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetDocumentEvaluationRecruitmentListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetDocumentSelectionApplicationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyDocumentEvaluationInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyDocumentEvaluationQuery;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentFormDefinitionInfo;
import com.umc.product.recruitment.application.port.out.LoadApplicationListPort;
import com.umc.product.recruitment.application.port.out.LoadApplicationPartPreferencePort;
import com.umc.product.recruitment.application.port.out.LoadApplicationPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPartPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentSchedulePort;
import com.umc.product.recruitment.domain.Application;
import com.umc.product.recruitment.domain.ApplicationPartPreference;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.enums.PartKey;
import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import com.umc.product.recruitment.domain.exception.RecruitmentDomainException;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.survey.application.port.in.query.dto.AnswerInfo;
import com.umc.product.survey.application.port.in.query.dto.FormDefinitionInfo;
import com.umc.product.survey.application.port.out.LoadFormPort;
import com.umc.product.survey.application.port.out.LoadFormResponsePort;
import com.umc.product.survey.domain.FormResponse;
import com.umc.product.survey.domain.SingleAnswer;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentDocumentEvaluationQueryService implements GetApplicationDetailUseCase,
    GetApplicationListUseCase,
    GetApplicationEvaluationListUseCase,
    GetMyDocumentEvaluationUseCase,
    GetDocumentSelectionListUseCase,
    GetDocumentEvaluationRecruitmentListUseCase {

    private final LoadApplicationListPort loadApplicationListPort;
    private final LoadApplicationPartPreferencePort loadApplicationPartPreferencePort;
    private final LoadRecruitmentPort loadRecruitmentPort;
    private final LoadApplicationPort loadApplicationPort;
    private final LoadMemberPort loadMemberPort;
    private final LoadFormPort loadFormPort;
    private final LoadFormResponsePort loadFormResponsePort;
    private final GetFileUseCase getFileUseCase;
    private final AnswerInfoMapper answerInfoMapper;
    private final ApplicationDetailMapper applicationDetailMapper;
    private final LoadGisuPort loadGisuPort;
    private final LoadRecruitmentSchedulePort loadRecruitmentSchedulePort;
    private final LoadRecruitmentPartPort loadRecruitmentPartPort;

    private Long resolveActiveGisuId() {
        return loadGisuPort.findActiveGisu().getId();
    }

    @Override
    public ApplicationDetailInfo get(GetApplicationDetailQuery query) {
        Recruitment currentRecruitment = loadRecruitmentPort.findById(query.recruitmentId())
            .orElseThrow(() -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));
        Long rootId = currentRecruitment.getEffectiveRootId();

        // todo: 운영진 권한 검증

        Application application = loadApplicationPort.findById(query.applicationId())
            .orElseThrow(() -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.APPLICATION_NOT_FOUND));

        if (!application.getRecruitment().getEffectiveRootId().equals(rootId)) {
            throw new BusinessException(Domain.RECRUITMENT,
                RecruitmentErrorCode.APPLICATION_NOT_BELONGS_TO_RECRUITMENT);
        }

        Member applicant = loadMemberPort.findById(application.getApplicantMemberId())
            .orElseThrow(() -> new BusinessException(Domain.MEMBER, MemberErrorCode.MEMBER_NOT_FOUND));

        Long formId = currentRecruitment.getFormId();
        if (formId == null) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND);
        }
        FormDefinitionInfo formDefinition = loadFormPort.loadFormDefinition(formId);

        RecruitmentFormDefinitionInfo recruitmentDef = RecruitmentFormDefinitionInfo.from(formDefinition);

        FormResponse formResponse = loadFormResponsePort.findById(application.getFormResponseId())
            .orElseThrow(() -> new BusinessException(Domain.SURVEY, SurveyErrorCode.FORM_RESPONSE_NOT_FOUND));

        List<AnswerInfo> answers = (formResponse.getAnswers() == null ? List.<SingleAnswer>of()
            : formResponse.getAnswers())
            .stream()
            .map(answerInfoMapper::toAnswerInfoWithPresignedUrlIfNeeded)
            .toList();

        // 지원자 파트 선호 로드 (priority 순)
        List<ApplicationPartPreference> prefs = loadApplicationPartPreferencePort.findByApplicationId(
            application.getId());

        return applicationDetailMapper.map(
            application,
            applicant,
            formDefinition,
            recruitmentDef,
            answers,
            prefs
        );
    }

    @Override
    public ApplicationListInfo get(GetApplicationListQuery query) {
        // todo: 운영진 권한 검증 필요

        Long recruitmentId = query.recruitmentId();
        String keyword = query.keyword();

        PartOption requestedPart = (query.part() != null) ? query.part() : PartOption.ALL;
        String part = requestedPart.getCode();

        Long evaluatorId = query.requesterMemberId();
        Pageable pageable = PageRequest.of(query.page(), query.size());

        // 1. Summary 정보 조회 (전체 지원자 수, 평가 완료된 지원자 수)
        long totalCount = loadApplicationListPort.countTotalApplications(recruitmentId);
        long evaluatedCount = loadApplicationListPort.countEvaluatedApplications(recruitmentId, evaluatorId);
        ApplicationListInfo.SummaryInfo summary = new ApplicationListInfo.SummaryInfo(totalCount, evaluatedCount);

        // 2. 페이지네이션된 지원서 목록 조회
        Page<ApplicationListItemProjection> applicationPage = loadApplicationListPort.searchApplications(
            recruitmentId, keyword, part, evaluatorId, pageable
        );

        // 3. 조회된 지원서들의 파트 선호도 일괄 조회
        Set<Long> applicationIds = applicationPage.getContent().stream()
            .map(ApplicationListItemProjection::applicationId)
            .collect(Collectors.toSet());

        Map<Long, List<ApplicationPartPreference>> partPreferenceMap = loadApplicationPartPreferencePort
            .findAllByApplicationIdsOrderByPriorityAsc(applicationIds)
            .stream()
            .collect(Collectors.groupingBy(pref -> pref.getApplication().getId()));

        // 4. ApplicationSummary 목록 생성
        List<ApplicationListInfo.ApplicationSummary> applicationSummaries = applicationPage.getContent().stream()
            .map(projection -> toApplicationSummary(projection, partPreferenceMap))
            .toList();

        // 5. PaginationInfo 생성
        ApplicationListInfo.PaginationInfo paginationInfo = new ApplicationListInfo.PaginationInfo(
            applicationPage.getNumber(),
            applicationPage.getSize(),
            applicationPage.getTotalPages(),
            applicationPage.getTotalElements()
        );

        return new ApplicationListInfo(recruitmentId, summary, applicationSummaries, paginationInfo);
    }

    private ApplicationListInfo.ApplicationSummary toApplicationSummary(
        ApplicationListItemProjection projection,
        Map<Long, List<ApplicationPartPreference>> partPreferenceMap
    ) {
        List<ApplicationPartPreference> preferences = partPreferenceMap.getOrDefault(
            projection.applicationId(), List.of()
        );

        List<ApplicationListInfo.PreferredPartInfo> preferredParts = preferences.stream()
            .map(pref -> new ApplicationListInfo.PreferredPartInfo(
                pref.getPriority(),
                toPartOption(pref.getRecruitmentPart().getPart())
            ))
            .toList();

        return new ApplicationListInfo.ApplicationSummary(
            projection.applicationId(),
            projection.applicantMemberId(),
            projection.applicantName(),
            projection.applicantNickname(),
            preferredParts,
            projection.isEvaluated()
        );
    }

    private PartOption toPartOption(ChallengerPart challengerPart) {
        return PartOption.valueOf(challengerPart.name());
    }

    @Override
    public ApplicationEvaluationListInfo get(GetApplicationEvaluationListQuery query) {
        // todo: 운영진 권한 검증 필요

        Long recruitmentId = query.recruitmentId();
        Long applicationId = query.applicationId();

        // 1. 해당 지원서가 이 모집에 속하는지 검증
        if (!loadApplicationListPort.isApplicationBelongsToRecruitment(applicationId, recruitmentId)) {
            throw new RecruitmentDomainException(RecruitmentErrorCode.APPLICATION_NOT_BELONGS_TO_RECRUITMENT);
        }

        // 2. 서류 평가 목록 조회
        List<EvaluationListItemProjection> evaluations =
            loadApplicationListPort.findDocumentEvaluationsByApplicationId(applicationId);

        // 3. 서류 평가 평균 점수 조회
        BigDecimal avgDocScore = loadApplicationListPort.calculateAvgDocScoreByApplicationId(applicationId);

        // 4. 응답 DTO 변환
        List<ApplicationEvaluationListInfo.DocEvaluationSummary> docEvaluationSummaries = evaluations.stream()
            .map(e -> new ApplicationEvaluationListInfo.DocEvaluationSummary(
                e.evaluationId(),
                e.evaluatorName(),
                e.evaluatorNickname(),
                e.score(),
                e.comments()
            ))
            .toList();

        return new ApplicationEvaluationListInfo(
            recruitmentId,
            applicationId,
            avgDocScore,
            docEvaluationSummaries
        );
    }

    @Override
    public GetMyDocumentEvaluationInfo get(GetMyDocumentEvaluationQuery query) {
        // todo: 본인 검증 필요

        Long recruitmentId = query.recruitmentId();
        Long applicationId = query.applicationId();
        Long evaluatorMemberId = query.evaluatorMemberId();

        // 1. 해당 지원서가 이 모집에 속하는지 검증
        if (!loadApplicationListPort.isApplicationBelongsToRecruitment(applicationId, recruitmentId)) {
            throw new RecruitmentDomainException(RecruitmentErrorCode.APPLICATION_NOT_BELONGS_TO_RECRUITMENT);
        }

        // 2. 내 서류 평가 조회
        return loadApplicationListPort.findMyDocumentEvaluation(applicationId, evaluatorMemberId)
            .map(projection -> new GetMyDocumentEvaluationInfo(
                new GetMyDocumentEvaluationInfo.MyDocumentEvaluationInfo(
                    projection.applicationId(),
                    projection.evaluationId(),
                    projection.score(),
                    projection.comments(),
                    projection.isSubmitted(),
                    projection.updatedAt()
                )
            ))
            .orElse(new GetMyDocumentEvaluationInfo(null));
    }

    @Override
    public DocumentSelectionApplicationListInfo get(GetDocumentSelectionApplicationListQuery query) {
        // todo: 운영진 권한 및 학교 체크
        // todo: 서류 평가 기간 검증

        Long recruitmentId = query.recruitmentId();
        PartOption part = query.part();
        SortOption sort = query.sort();

        Pageable pageable = PageRequest.of(query.page(), query.size());

        // summary
        DocumentSelectionApplicationListInfo.Summary summary =
            loadApplicationListPort.getDocumentSelectionSummary(recruitmentId, part.name());

        // 페이지 단위 목록 조회
        Page<DocumentSelectionListItemProjection> page = loadApplicationListPort.searchDocumentSelections(
            recruitmentId,
            part.name(),
            sort.name(),
            pageable
        );

        // 대상 applicationIds
        Set<Long> applicationIds = page.getContent().stream()
            .map(DocumentSelectionListItemProjection::applicationId)
            .collect(Collectors.toSet());

        // 비어있으면 빈 리스트 리턴
        if (applicationIds.isEmpty()) {
            return new DocumentSelectionApplicationListInfo(
                summary,
                sort.name(),
                List.of(),
                new DocumentSelectionApplicationListInfo.PaginationInfo(
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalPages(),
                    page.getTotalElements(),
                    page.hasNext(),
                    page.hasPrevious()
                )
            );
        }

        // appliedParts (priority 순)
        Map<Long, List<ApplicationPartPreference>> prefMap =
            loadApplicationPartPreferencePort.findAllByApplicationIdsOrderByPriorityAsc(applicationIds)
                .stream()
                .collect(Collectors.groupingBy(pref -> pref.getApplication().getId()));

        // avg docScore 일괄 로드
        Map<Long, BigDecimal> avgDocScoreMap =
            loadApplicationListPort.calculateAvgDocScoreByApplicationIds(applicationIds);

        // 응답 item 매핑
        List<DocumentSelectionApplicationListInfo.DocumentSelectionApplicationInfo> items =
            page.getContent().stream()
                .map(p -> toDocumentSelectionApplicationInfoItem(p, prefMap, avgDocScoreMap))
                .toList();

        // pagination
        DocumentSelectionApplicationListInfo.PaginationInfo pagination =
            new DocumentSelectionApplicationListInfo.PaginationInfo(
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext(),
                page.hasPrevious()
            );

        return new DocumentSelectionApplicationListInfo(
            summary,
            sort.name(),
            items,
            pagination
        );
    }

    private DocumentSelectionApplicationListInfo.DocumentSelectionApplicationInfo toDocumentSelectionApplicationInfoItem(
        DocumentSelectionListItemProjection p,
        Map<Long, List<ApplicationPartPreference>> prefMap,
        Map<Long, BigDecimal> avgDocScoreMap
    ) {
        Long applicationId = p.applicationId();

        // appliedParts
        List<ApplicationPartPreference> prefs = prefMap.getOrDefault(applicationId, List.of());
        List<DocumentSelectionApplicationListInfo.AppliedPartInfo> appliedParts = prefs.stream()
            .map(pref -> new DocumentSelectionApplicationListInfo.AppliedPartInfo(
                pref.getPriority(),
                PartKey.valueOf(pref.getRecruitmentPart().getPart().name())
            ))
            .toList();

        // documentScore
        BigDecimal avg = avgDocScoreMap.get(applicationId);
        Double documentScore = (avg == null) ? null : avg.doubleValue();

        // documentResult decision
        EvaluationDecision decision = switch (p.status()) {
            case DOC_PASSED -> EvaluationDecision.PASS;
            case DOC_FAILED -> EvaluationDecision.FAIL;
            case APPLIED -> EvaluationDecision.WAIT;
            default -> {
                log.error("[DocumentSelection] unexpected status. appId={}, status={}", applicationId, p.status());
                yield EvaluationDecision.WAIT;
            }
        };

        return new DocumentSelectionApplicationListInfo.DocumentSelectionApplicationInfo(
            applicationId,
            new DocumentSelectionApplicationListInfo.ApplicantInfo(
                p.nickname(),
                p.name()
            ),
            appliedParts,
            documentScore,
            new DocumentSelectionApplicationListInfo.DocumentResult(decision.name())
        );
    }

    @Override
    public DocumentEvaluationRecruitmentListInfo get(GetDocumentEvaluationRecruitmentListQuery query) {
        // todo: 운영진 권한 검증 필요

        Member requester = loadMemberPort.findById(query.memberId())
            .orElseThrow(() -> new BusinessException(Domain.MEMBER, MemberErrorCode.MEMBER_NOT_FOUND));

        Long schoolId = requester.getSchoolId();
        if (schoolId == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND);
        }

        Long gisuId = resolveActiveGisuId();
        // 1) 후보 recruitment 목록 조회 (PUBLISHED + schoolId + gisuId)
        List<Recruitment> recruitments = loadRecruitmentPort.findAllPublishedBySchoolIdAndGisuId(schoolId, gisuId);

        // 2) evaluatingList: DOC_RESULT_AT 기준으로 "결과 발표 전"만 필터링
        //    completeList: DOC_RESULT_AT 이후, 최종 발표 전 핕터링
        Instant now = Instant.now();

        List<DocumentEvaluationRecruitmentInfo> evaluatingList = new ArrayList<>();
        List<DocumentEvaluationRecruitmentInfo> completeList = new ArrayList<>();

        for (Recruitment recruitment : recruitments) {
            Long recruitmentId = recruitment.getId();

            // 1. 기준이 되는 날짜 조회 (서류 결과, 최종 결과)
            var docResultSchedule = loadRecruitmentSchedulePort
                .findOptionalByRecruitmentIdAndType(recruitmentId, RecruitmentScheduleType.DOC_RESULT_AT)
                .orElse(null);

            var finalResultSchedule = loadRecruitmentSchedulePort
                .findOptionalByRecruitmentIdAndType(recruitmentId, RecruitmentScheduleType.FINAL_RESULT_AT)
                .orElse(null);

            // 2. 화면 표시용 (모집 시작일)
            var appStartSchedule = loadRecruitmentSchedulePort
                .findOptionalByRecruitmentIdAndType(recruitmentId, RecruitmentScheduleType.APPLY_WINDOW)
                .orElse(null);

            // 날짜 객체 추출 (null 안전 처리)
            Instant docResultAt = (docResultSchedule != null) ? docResultSchedule.getStartsAt() : null;
            Instant finalResultAt = (finalResultSchedule != null) ? finalResultSchedule.getStartsAt() : null;
            Instant appStartAt = (appStartSchedule != null) ? appStartSchedule.getStartsAt() : null;

            if (docResultAt == null) {
                continue;
            }

            DocumentEvaluationRecruitmentInfo info = new DocumentEvaluationRecruitmentInfo(
                recruitmentId,
                recruitment.getEffectiveRootId(), // 본모집 ID(또는 자기자신 ID) 전달
                recruitment.getTitle(),
                appStartAt,    // 모집 시작
                finalResultAt, // 최종 발표
                loadApplicationListPort.countTotalApplications(recruitmentId),
                getOpenParts(recruitmentId) // 파트 조회
            );

            // Case 1: 서류 결과 발표 전 -> [평가 중]
            if (now.isBefore(docResultAt)) {
                evaluatingList.add(info);
            }
            // Case 2: 서류 결과 발표 후 ~ 최종 발표 전 -> [평가 완료]
            else if (finalResultAt == null || now.isBefore(finalResultAt)) {
                completeList.add(info);
            }
            // Case 3: 최종 발표 후 -> [목록 제외 (Hidden)]
            else {
            }
        }

        return new DocumentEvaluationRecruitmentListInfo(evaluatingList, completeList);
    }

    private List<PartKey> getOpenParts(Long recruitmentId) {
        return loadRecruitmentPartPort.findOpenPartsByRecruitmentId(recruitmentId).stream()
            .map(challengerPart -> PartKey.valueOf(challengerPart.name()))
            .toList();
    }
}
