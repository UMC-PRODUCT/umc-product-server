package com.umc.product.recruitment.application.service.query;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import com.umc.product.recruitment.application.port.in.query.GetActiveRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.query.GetMyApplicationListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentApplicationFormUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentDashboardUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentDetailUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentFormResponseDetailUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentNoticeUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentPartListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentScheduleUseCase;
import com.umc.product.recruitment.application.port.in.query.RecruitmentListStatus;
import com.umc.product.recruitment.application.port.in.query.dto.ActiveRecruitmentInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetActiveRecruitmentQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyApplicationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentApplicationFormQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentDetailQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentFormResponseDetailQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentNoticeQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentPartListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentScheduleQuery;
import com.umc.product.recruitment.application.port.in.query.dto.MyApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentDashboardInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentFormResponseDetailInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentNoticeInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentPartListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentScheduleInfo;
import com.umc.product.recruitment.application.port.out.LoadApplicationPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPartPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPort;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.RecruitmentPart;
import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import com.umc.product.survey.application.port.in.query.dto.AnswerInfo;
import com.umc.product.survey.application.port.out.LoadFormResponsePort;
import com.umc.product.survey.domain.FormResponse;
import com.umc.product.survey.domain.SingleAnswer;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentQueryService implements GetActiveRecruitmentUseCase, GetRecruitmentNoticeUseCase,
        GetRecruitmentApplicationFormUseCase,
        GetRecruitmentFormResponseDetailUseCase,
        GetRecruitmentListUseCase,
        GetRecruitmentScheduleUseCase,
        GetRecruitmentDashboardUseCase,
        GetMyApplicationListUseCase,
        GetRecruitmentDetailUseCase,
        GetRecruitmentPartListUseCase {

    private final LoadRecruitmentPort loadRecruitmentPort;
    private final LoadRecruitmentPartPort loadRecruitmentPartPort;
    private final LoadApplicationPort loadApplicationPort;
    private final LoadFormResponsePort loadFormResponsePort;

    @Override
    public ActiveRecruitmentInfo get(GetActiveRecruitmentQuery query) {
        return null;
    }

    @Override
    public RecruitmentNoticeInfo get(GetRecruitmentNoticeQuery query) {
        Recruitment recruitment = loadRecruitmentPort.findById(query.recruitmentId())
                .orElseThrow(
                        () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        requirePublished(recruitment);

        List<RecruitmentPart> recruitmentParts = loadRecruitmentPartPort.findByRecruitmentId(query.recruitmentId());

        List<com.umc.product.common.domain.enums.ChallengerPart> openParts = recruitmentParts.stream()
                .filter(p -> p.getStatus() != null && p.getStatus().name().equals("OPEN"))
                .map(RecruitmentPart::getPart)
                .sorted(java.util.Comparator.comparingInt(Enum::ordinal))
                .toList();

        String title = (recruitment.getNoticeTitle() != null && !recruitment.getNoticeTitle().isBlank())
                ? recruitment.getNoticeTitle()
                : recruitment.getTitle();

        return new RecruitmentNoticeInfo(
                recruitment.getId(),
                title,
                recruitment.getNoticeContent(),
                openParts
        );
    }

    @Override
    public RecruitmentApplicationFormInfo get(GetRecruitmentApplicationFormQuery query) {

        return null;
    }

    @Override
    public RecruitmentFormResponseDetailInfo get(GetRecruitmentFormResponseDetailQuery query) {
        Recruitment recruitment = loadRecruitmentPort.findById(query.recruitmentId())
                .orElseThrow(() ->
                        new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND)
                );

        if (!recruitment.isPublished()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_PUBLISHED);
        }

        Long formId = recruitment.getFormId();
        if (formId == null) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND);
        }

        FormResponse formResponse = loadFormResponsePort.findById(query.formResponseId())
                .orElseThrow(() ->
                        new BusinessException(Domain.SURVEY, SurveyErrorCode.FORM_RESPONSE_NOT_FOUND)
                );

        if (formResponse.getForm() == null || formResponse.getForm().getId() == null
                || !formId.equals(formResponse.getForm().getId())) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_FORM_MISMATCH);
        }

        if (query.memberId() != null && !query.memberId().equals(formResponse.getRespondentMemberId())) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.FORM_RESPONSE_FORBIDDEN);
        }

        List<AnswerInfo> answers = (formResponse.getAnswers() == null ? List.<SingleAnswer>of()
                : formResponse.getAnswers())
                .stream()
                .map(a -> new AnswerInfo(
                        a.getQuestion().getId(),
                        a.getValue(),
                        a.getAnsweredAsType()
                ))
                .toList();

        return new RecruitmentFormResponseDetailInfo(
                formId,
                formResponse.getId(),
                formResponse.getStatus(),
                formResponse.getUpdatedAt(),
                formResponse.getSubmittedAt(),
                answers
        );
    }

    @Override
    public RecruitmentListInfo getList(GetRecruitmentListQuery query) {

        if (query.status() == RecruitmentListStatus.DRAFT) {
            return new RecruitmentListInfo(
                    loadRecruitmentPort.findDraftRecruitmentSummaries(query.requesterMemberId())
            );
        }

        List<RecruitmentListInfo.RecruitmentSummary> summaries =
                loadRecruitmentPort.findRecruitmentSummaries(
                        query.requesterMemberId(),
                        query.status()
                );

        return new RecruitmentListInfo(summaries);
    }

    @Override
    public RecruitmentScheduleInfo get(GetRecruitmentScheduleQuery query) {
        Recruitment recruitment = loadRecruitmentPort.findById(query.recruitmentId())
                .orElseThrow(
                        () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        requirePublished(recruitment);

        var schedules = loadRecruitmentPort.findSchedulesByRecruitmentId(query.recruitmentId());

        var scheduleItems = schedules.stream()
                .map(schedule -> new RecruitmentScheduleInfo.ScheduleItem(
                        schedule.getType(),
                        schedule.getType().kind() == RecruitmentScheduleType.Kind.WINDOW
                                ? RecruitmentScheduleInfo.ScheduleKind.WINDOW
                                : RecruitmentScheduleInfo.ScheduleKind.AT,
                        schedule.getStartsAt(),
                        schedule.getEndsAt()
                ))
                .toList();

        return new RecruitmentScheduleInfo(query.recruitmentId(), scheduleItems);
    }

    @Override
    public RecruitmentDashboardInfo get(Long recruitmentId) {
        return null;
    }

    @Override
    public MyApplicationListInfo get(GetMyApplicationListQuery query) {
        return null;
    }

    @Override
    public RecruitmentDraftInfo get(GetRecruitmentDetailQuery query) {
        loadRecruitmentPort.findById(query.recruitmentId())
                .orElseThrow(
                        () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        // TODO: 권한 검증 필요 (memberId 기반)

        return loadRecruitmentPort.findDraftInfoById(query.recruitmentId());
    }

    @Override
    public RecruitmentPartListInfo get(GetRecruitmentPartListQuery query) {
        Recruitment recruitment = loadRecruitmentPort.findById(query.recruitmentId())
                .orElseThrow(
                        () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        List<RecruitmentPart> recruitmentParts = loadRecruitmentPartPort.findByRecruitmentId(query.recruitmentId());

        var partSummaries = recruitmentParts.stream()
                .map(part -> new RecruitmentPartListInfo.RecruitmentPartSummary(
                        part.getId(),
                        part.getPart(),
                        part.getStatus()
                ))
                .toList();

        // 지원자의 해당 모집 지원 현황 조회 (없으면 null)
        RecruitmentPartListInfo.MyApplicationInfo myApplicationInfo = loadApplicationPort
                .findByRecruitmentIdAndApplicantId(query.recruitmentId(), query.memberId())
                .map(app -> new RecruitmentPartListInfo.MyApplicationInfo(
                        app.getId(),
                        app.getFormResponseId(),
                        app.getStatus()
                ))
                .orElse(null);

        var schedules = loadRecruitmentPort.findSchedulesByRecruitmentId(query.recruitmentId());
        var recruitmentPeriod = extractDatePeriod(schedules, "APPLY_WINDOW");
        var activityPeriod = extractDatePeriod(schedules, "ACTIVITY_WINDOW");

        return new RecruitmentPartListInfo(
                recruitment.getId(),
                recruitment.getTitle(),
                recruitmentPeriod,
                activityPeriod,
                recruitment.getNoticeContent(),
                partSummaries,
                myApplicationInfo
        );
    }

    private RecruitmentPartListInfo.DatePeriod extractDatePeriod(
            java.util.List<com.umc.product.recruitment.domain.RecruitmentSchedule> schedules,
            String scheduleType) {
        return schedules.stream()
                .filter(schedule -> schedule.getType().name().equals(scheduleType))
                .findFirst()
                .map(schedule -> new RecruitmentPartListInfo.DatePeriod(
                        schedule.getStartsAt(),
                        schedule.getEndsAt()
                ))
                .orElse(null);
    }

    private void requirePublished(Recruitment recruitment) {
        if (recruitment.getStatus() != com.umc.product.recruitment.domain.enums.RecruitmentStatus.PUBLISHED) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_PUBLISHED);
        }
    }
}
