package com.umc.product.recruitment.application.service.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberErrorCode;
import com.umc.product.recruitment.adapter.in.web.mapper.AnswerInfoMapper;
import com.umc.product.recruitment.adapter.in.web.mapper.ApplicationDetailMapper;
import com.umc.product.recruitment.adapter.out.dto.ApplicationListItemProjection;
import com.umc.product.recruitment.adapter.out.dto.EvaluationListItemProjection;
import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.in.query.GetApplicationDetailUseCase;
import com.umc.product.recruitment.application.port.in.query.GetApplicationEvaluationListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetApplicationListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetDocumentSelectionListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetMyDocumentEvaluationUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationDetailInfo;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationEvaluationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.DocumentSelectionApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationDetailQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationEvaluationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetDocumentSelectionApplicationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyDocumentEvaluationInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyDocumentEvaluationQuery;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentFormDefinitionInfo;
import com.umc.product.recruitment.application.port.out.LoadApplicationListPort;
import com.umc.product.recruitment.application.port.out.LoadApplicationPartPreferencePort;
import com.umc.product.recruitment.application.port.out.LoadApplicationPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPort;
import com.umc.product.recruitment.domain.Application;
import com.umc.product.recruitment.domain.ApplicationPartPreference;
import com.umc.product.recruitment.domain.Recruitment;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentDocumentEvaluationQueryService implements GetApplicationDetailUseCase,
    GetApplicationListUseCase,
    GetApplicationEvaluationListUseCase,
    GetMyDocumentEvaluationUseCase,
    GetDocumentSelectionListUseCase {

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

    @Override
    public ApplicationDetailInfo get(GetApplicationDetailQuery query) {
        Recruitment recruitment = loadRecruitmentPort.findById(query.recruitmentId())
            .orElseThrow(() -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        // todo: 운영진 권한 검증

        Application application = loadApplicationPort.getByRecruitmentIdAndApplicationId(
            query.recruitmentId(),
            query.applicationId()
        ).orElseThrow(() -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.APPLICATION_NOT_FOUND));

        Member applicant = loadMemberPort.findById(application.getApplicantMemberId())
            .orElseThrow(() -> new BusinessException(Domain.MEMBER, MemberErrorCode.MEMBER_NOT_FOUND));

        Long formId = recruitment.getFormId();
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
        String part = query.part();
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
        return null;
    }
}
