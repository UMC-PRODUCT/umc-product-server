package com.umc.product.recruitment.application.service.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberErrorCode;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentPublishedInfo;
import com.umc.product.recruitment.application.port.in.query.GetActiveRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.query.GetMyApplicationListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetPublishedRecruitmentDetailUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentApplicationFormUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentDashboardUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentDetailUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentDraftApplicationFormUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentFormResponseDetailUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentNoticeUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentPartListUseCase;
import com.umc.product.recruitment.application.port.in.query.GetRecruitmentScheduleUseCase;
import com.umc.product.recruitment.application.port.in.query.RecruitmentListStatus;
import com.umc.product.recruitment.application.port.in.query.dto.ActiveRecruitmentInfo;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationProgressNoticeType;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationProgressStep;
import com.umc.product.recruitment.application.port.in.query.dto.EvaluationStatusCode;
import com.umc.product.recruitment.application.port.in.query.dto.GetActiveRecruitmentQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyApplicationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetPublishedRecruitmentDetailQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentApplicationFormQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentDetailQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetRecruitmentDraftApplicationFormQuery;
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
import com.umc.product.recruitment.application.port.out.LoadApplicationPartPreferencePort;
import com.umc.product.recruitment.application.port.out.LoadApplicationPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPartPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPort;
import com.umc.product.recruitment.domain.Application;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.RecruitmentPart;
import com.umc.product.recruitment.domain.RecruitmentSchedule;
import com.umc.product.recruitment.domain.enums.ApplicationStatus;
import com.umc.product.recruitment.domain.enums.RecruitmentPartStatus;
import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import com.umc.product.recruitment.domain.enums.RecruitmentStatus;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileInfo;
import com.umc.product.survey.application.port.in.query.dto.AnswerInfo;
import com.umc.product.survey.application.port.out.LoadFormResponsePort;
import com.umc.product.survey.domain.FormResponse;
import com.umc.product.survey.domain.SingleAnswer;
import com.umc.product.survey.domain.enums.QuestionType;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
        GetRecruitmentPartListUseCase,
        GetRecruitmentDraftApplicationFormUseCase,
        GetPublishedRecruitmentDetailUseCase {

    private final LoadRecruitmentPort loadRecruitmentPort;
    private final LoadRecruitmentPartPort loadRecruitmentPartPort;
    private final LoadApplicationPort loadApplicationPort;
    private final LoadFormResponsePort loadFormResponsePort;
    private final LoadMemberPort loadMemberPort;
    private final LoadApplicationPartPreferencePort loadApplicationPartPreferencePort;
    private final LoadGisuPort loadGisuPort;
    private final GetFileUseCase getFileUseCase;

    private Long resolveSchoolId(Long memberId) {
        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(Domain.MEMBER, MemberErrorCode.MEMBER_NOT_FOUND));
        return member.getSchoolId();
    }

    private Long resolveActiveGisuId() {
        return loadGisuPort.findActiveGisu().getId();
    }

    @Override
    public ActiveRecruitmentInfo getActiveRecruitment(GetActiveRecruitmentQuery query) {
        Long resolvedSchoolId =
                (query.schoolId() != null) ? query.schoolId() : resolveSchoolId(query.requesterMemberId());
        Long resolvedGisuId = (query.gisuId() != null) ? query.gisuId() : resolveActiveGisuId();

        List<Long> activeIds = loadRecruitmentPort.findActiveRecruitmentIds(
                resolvedSchoolId,
                resolvedGisuId,
                Instant.now()
        );

        if (activeIds.isEmpty()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND);
        }

        if (activeIds.size() > 1) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.MULTIPLE_ACTIVE_RECRUITMENTS);
        }

        return new ActiveRecruitmentInfo(activeIds.get(0));
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
                .sorted(Comparator.comparingInt(Enum::ordinal))
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
        Recruitment recruitment = loadRecruitmentPort.findById(query.recruitmentId())
                .orElseThrow(
                        () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        List<RecruitmentPart> parts = loadRecruitmentPartPort.findByRecruitmentId(query.recruitmentId());

        List<RecruitmentApplicationFormInfo.PreferredPartInfo.PreferredPartOptionInfo> preferredPartOptions =
                (parts == null ? List.<RecruitmentPart>of() : parts).stream()
                        .filter(p -> p.getStatus() == RecruitmentPartStatus.OPEN)
                        .map(p -> new RecruitmentApplicationFormInfo.PreferredPartInfo.PreferredPartOptionInfo(
                                p.getId(),
                                p.getPart().name(),
                                p.getPart().name()
                        ))
                        .toList();

        Integer max = recruitment.getMaxPreferredPartCount();
        if (max == null) {
            max = 1;
        }

        var preferredPartInfo =
                new RecruitmentApplicationFormInfo.PreferredPartInfo(max, preferredPartOptions);

        if (recruitment.isPublished()) {
            return loadRecruitmentPort.findApplicationFormInfoForApplicantById(query.recruitmentId(),
                    preferredPartInfo);
        }

        // TODO: 운영진 권한 검증 추가 (DRAFT면 운영진만 허용)

        return loadRecruitmentPort.findApplicationFormInfoForApplicantById(query.recruitmentId(), preferredPartInfo);
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
                .map(this::toAnswerInfoWithPresignedUrlIfNeeded)
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
        Long memberId = query.memberId();

        Member member = loadMemberPort.findById(memberId)
                .orElseThrow(() -> new BusinessException(Domain.MEMBER, MemberErrorCode.MEMBER_NOT_FOUND));

        String nickName = member.getNickname();
        String name = member.getName();

        Long schoolId = resolveSchoolId(memberId);
        Long activeGisuId = resolveActiveGisuId();

        Long activeRecruitmentId = loadRecruitmentPort.findActiveRecruitmentId(
                schoolId, activeGisuId, Instant.now()
        ).orElse(null);

        List<FormResponse> drafts = loadFormResponsePort.findAllDraftByRespondentMemberId(memberId);
        if (drafts == null) {
            drafts = List.of();
        }

        List<Application> apps = loadApplicationPort.findAllByApplicantMemberId(memberId);
        if (apps == null) {
            apps = List.of();
        }

        CurrentPicked currentPicked = pickCurrent(activeRecruitmentId, drafts, apps);

        MyApplicationListInfo.CurrentApplicationStatusInfo current =
                buildCurrentOrBeforeApply(activeRecruitmentId, currentPicked);

        List<MyApplicationListInfo.MyApplicationCardInfo> cards =
                buildCards(activeRecruitmentId, drafts, apps, currentPicked);

        return new MyApplicationListInfo(nickName, name, current, cards);
    }

    /**
     * 활성 모집 기준 current 선택 - activeRecruitmentId 없으면 current 없음 - 활성 모집 submitted 있으면 submitted 우선 - 없으면 활성 모집 draft 있으면
     * draft - 둘 다 없으면 current 없음 (=> 지금 열린 모집에 지원 안했으면 리스트에 없음)
     */
    private CurrentPicked pickCurrent(Long activeRecruitmentId, List<FormResponse> drafts, List<Application> apps) {
        if (activeRecruitmentId == null) {
            return null;
        }

        // submitted 우선
        Application activeApp = (apps == null ? List.<Application>of() : apps).stream()
                .filter(a -> a.getRecruitment() != null
                        && activeRecruitmentId.equals(a.getRecruitment().getId()))
                .max(Comparator.comparing(
                        Application::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .orElse(null);

        if (activeApp != null) {
            return CurrentPicked.submitted(activeApp.getRecruitment(), activeApp);
        }

        FormResponse activeDraft = (drafts == null ? List.<FormResponse>of() : drafts).stream()
                .filter(fr -> isRecruitmentMatchedByFormId(activeRecruitmentId, fr))
                .max(Comparator.comparing(
                        FormResponse::getUpdatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .orElse(null);

        if (activeDraft != null) {
            Long formId = activeDraft.getForm() == null ? null : activeDraft.getForm().getId();
            Recruitment r = (formId == null) ? null
                    : loadRecruitmentPort.findByFormId(formId).orElse(null);
            if (r != null) {
                return CurrentPicked.draft(r, activeDraft);
            }
        }

        return null;
    }

    /**
     * 지원 전
     */
    private MyApplicationListInfo.CurrentApplicationStatusInfo buildCurrentOrBeforeApply(
            Long activeRecruitmentId,
            CurrentPicked currentPicked
    ) {
        if (activeRecruitmentId == null) {
            return null;
        }

        // 지원이 있을 때
        if (currentPicked != null) {
            return buildCurrentStatus(currentPicked);
        }

        // 아직 지원하지 않았을 때
        ProgressComputed progress = resolveProgressForUI(activeRecruitmentId, null);

        List<String> appliedParts = List.of();

        MyApplicationListInfo.EvaluationStatusInfo docEval =
                new MyApplicationListInfo.EvaluationStatusInfo(EvaluationStatusCode.PENDING);
        MyApplicationListInfo.EvaluationStatusInfo finalEval =
                new MyApplicationListInfo.EvaluationStatusInfo(EvaluationStatusCode.PENDING);

        return new MyApplicationListInfo.CurrentApplicationStatusInfo(
                appliedParts,
                docEval,
                finalEval,
                progress.timeline
        );
    }

    /**
     * 지원서 카드 리스트 구성 - current가 있으면 맨 위에 1개 (DRAFT or SUBMITTED) - 나머지 draft는 DRAFT, 나머지 submitted는 전부 PAST
     */
    private List<MyApplicationListInfo.MyApplicationCardInfo> buildCards(
            Long activeRecruitmentId,
            List<FormResponse> drafts,
            List<Application> apps,
            CurrentPicked currentPicked
    ) {
        List<MyApplicationListInfo.MyApplicationCardInfo> result = new java.util.ArrayList<>();

        if (currentPicked != null) {
            result.add(toCurrentCard(currentPicked));
        }

        // 활성 모집에 해당하는 draft/app 최신 1개
        Long currentDraftFormResponseId = (currentPicked != null && currentPicked.kind == CurrentPicked.Kind.DRAFT)
                ? currentPicked.formResponse.getId()
                : null;

        Long currentSubmittedAppId = (currentPicked != null && currentPicked.kind == CurrentPicked.Kind.SUBMITTED)
                ? currentPicked.application.getId()
                : null;

        Long latestActiveDraftId = (activeRecruitmentId == null) ? null
                : (drafts == null ? List.<FormResponse>of() : drafts).stream()
                        .filter(fr -> isRecruitmentMatchedByFormId(activeRecruitmentId, fr))
                        .max(java.util.Comparator.comparing(
                                FormResponse::getUpdatedAt,
                                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
                        ))
                        .map(FormResponse::getId)
                        .orElse(null);

        Long latestActiveSubmittedAppId = (activeRecruitmentId == null) ? null
                : (apps == null ? List.<Application>of() : apps).stream()
                        .filter(a -> a.getRecruitment() != null && activeRecruitmentId.equals(
                                a.getRecruitment().getId()))
                        .max(java.util.Comparator.comparing(
                                Application::getCreatedAt,
                                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
                        ))
                        .map(Application::getId)
                        .orElse(null);

        List<MyApplicationListInfo.MyApplicationCardInfo> draftCards =
                (drafts == null ? List.<FormResponse>of() : drafts).stream()
                        .map(fr -> {
                            Long formId = (fr.getForm() == null) ? null : fr.getForm().getId();
                            if (formId == null) {
                                return null;
                            }

                            Recruitment recruitment = loadRecruitmentPort.findByFormId(formId).orElse(null);
                            if (recruitment == null) {
                                return null;
                            }

                            if (currentDraftFormResponseId != null && currentDraftFormResponseId.equals(fr.getId())) {
                                return null;
                            }

                            if (activeRecruitmentId != null
                                    && recruitment.getId() != null
                                    && recruitment.getId().equals(activeRecruitmentId)) {
                                if (latestActiveDraftId != null && !latestActiveDraftId.equals(fr.getId())) {
                                    return null;
                                }
                            }

                            return new MyApplicationListInfo.MyApplicationCardInfo(
                                    recruitment.getId(),
                                    null,
                                    fr.getId(),
                                    recruitment.getTitle(),
                                    "DRAFT",
                                    null,
                                    fr.getUpdatedAt()
                            );
                        })
                        .filter(java.util.Objects::nonNull)
                        .sorted((a, b) -> compareInstantDesc(a.submittedAt(),
                                b.submittedAt())) // DRAFT: updatedAt, SUBMITTED/PAST: createdAt 기준 정렬
                        .toList();

        List<MyApplicationListInfo.MyApplicationCardInfo> submittedCards =
                (apps == null ? List.<Application>of() : apps).stream()
                        .map(app -> {
                            Recruitment recruitment = app.getRecruitment();
                            if (recruitment == null) {
                                return null;
                            }

                            if (currentSubmittedAppId != null && currentSubmittedAppId.equals(app.getId())) {
                                return null;
                            }

                            if (activeRecruitmentId != null
                                    && recruitment.getId() != null
                                    && recruitment.getId().equals(activeRecruitmentId)) {
                                if (latestActiveSubmittedAppId != null && !latestActiveSubmittedAppId.equals(
                                        app.getId())) {
                                    return null;
                                }
                            }

                            return new MyApplicationListInfo.MyApplicationCardInfo(
                                    recruitment.getId(),
                                    app.getId(),
                                    app.getFormResponseId(),
                                    recruitment.getTitle(),
                                    "PAST",
                                    app.getStatus(),
                                    app.getCreatedAt()
                            );
                        })
                        .filter(java.util.Objects::nonNull)
                        .sorted((a, b) -> compareInstantDesc(a.submittedAt(),
                                b.submittedAt())) // DRAFT: updatedAt, SUBMITTED/PAST: createdAt 기준 정렬
                        .toList();

        result.addAll(draftCards);
        result.addAll(submittedCards);
        return result;
    }

    private MyApplicationListInfo.MyApplicationCardInfo toCurrentCard(CurrentPicked picked) {
        if (picked.kind == CurrentPicked.Kind.DRAFT) {
            return new MyApplicationListInfo.MyApplicationCardInfo(
                    picked.recruitment.getId(),
                    null,
                    picked.formResponse.getId(),
                    picked.recruitment.getTitle(),
                    "DRAFT",
                    null,
                    picked.formResponse.getUpdatedAt()
            );
        }

        return new MyApplicationListInfo.MyApplicationCardInfo(
                picked.recruitment.getId(),
                picked.application.getId(),
                picked.application.getFormResponseId(),
                picked.recruitment.getTitle(),
                "SUBMITTED",
                picked.application.getStatus(),
                picked.application.getCreatedAt()
        );
    }

    /**
     * progress 계산 - progress(step/notice)는 schedule 기반으로만 계산 - docEval/finalEval은 발표 이후에만 EvaluationDecision을 DB에서 조회해
     * 노출 - 서류 FAIL: finalEval은 "예정 없음"
     */
    private MyApplicationListInfo.CurrentApplicationStatusInfo buildCurrentStatus(CurrentPicked picked) {

        ProgressComputed progress = resolveProgressForUI(
                picked.recruitment.getId(),
                picked.kind == CurrentPicked.Kind.SUBMITTED ? picked.application.getStatus() : null
        );

        if (picked.kind == CurrentPicked.Kind.DRAFT) {
            return new MyApplicationListInfo.CurrentApplicationStatusInfo(
                    List.of(),
                    new MyApplicationListInfo.EvaluationStatusInfo(EvaluationStatusCode.PENDING),
                    new MyApplicationListInfo.EvaluationStatusInfo(EvaluationStatusCode.PENDING),
                    progress.timeline
            );
        }

        // 지원서 제출 완료 시
        List<String> appliedParts = loadApplicationPartPreferencePort
                .findAllByApplicationIdOrderByPriorityAsc(picked.application.getId())
                .stream()
                .map(pp -> pp.getRecruitmentPart().getPart().name())
                .toList();

        MyApplicationListInfo.EvaluationStatusInfo docEval = buildDocEvalForUser(picked.application, progress);
        MyApplicationListInfo.EvaluationStatusInfo finalEval = buildFinalEvalForUser(picked.application, progress,
                docEval);

        return new MyApplicationListInfo.CurrentApplicationStatusInfo(
                appliedParts,
                docEval,
                finalEval,
                progress.timeline
        );
    }

    /**
     * 진행 계산 결과 묶음
     */
    private static record ProgressComputed(
            Instant now,
            ApplicationProgressStep currentStep,
            MyApplicationListInfo.ProgressTimelineInfo timeline,
            Instant applyEndAt,
            Instant docResultAt,
            Instant finalResultAt
    ) {
    }

    /**
     * schedule 기반 6단계 + noticeType/date 계산 - BEFORE_APPLY: noticeType=APPLY_DEADLINE, date=applyWindow.endsAt -
     * DOC_REVIEWING: noticeType=DOC_RESULT_ANNOUNCE, date=docResultAt - DOC_RESULT_PUBLISHED ~ FINAL_REVIEWING:
     * noticeType=FINAL_RESULT_ANNOUNCE, date=finalResultAt - FINAL_RESULT_PUBLISHED: noticeType/date = null (프론트 고정 문구
     * 출력)
     */
    // todo: phase 계산 공통 유틸로 빼기
    private ProgressComputed resolveProgressForUI(Long recruitmentId, ApplicationStatus appStatus) {
        List<RecruitmentSchedule> schedules = loadRecruitmentPort.findSchedulesByRecruitmentId(recruitmentId);
        if (schedules == null) {
            schedules = List.of();
        }

        Instant now = Instant.now();

        RecruitmentSchedule applyWindow = findSchedule(schedules, RecruitmentScheduleType.APPLY_WINDOW);
        RecruitmentSchedule docReviewWindow = findSchedule(schedules, RecruitmentScheduleType.DOC_REVIEW_WINDOW);
        RecruitmentSchedule docResultAt = findSchedule(schedules, RecruitmentScheduleType.DOC_RESULT_AT);
        RecruitmentSchedule interviewWindow = findSchedule(schedules, RecruitmentScheduleType.INTERVIEW_WINDOW);
        RecruitmentSchedule finalReviewWindow = findSchedule(schedules, RecruitmentScheduleType.FINAL_REVIEW_WINDOW);
        RecruitmentSchedule finalResultAt = findSchedule(schedules, RecruitmentScheduleType.FINAL_RESULT_AT);

        ApplicationProgressStep currentStep = computeCurrentStepForUI(
                now, applyWindow, docReviewWindow, docResultAt, interviewWindow, finalReviewWindow, finalResultAt
        );

        Instant docResultInstant = pickAt(docResultAt);
        Instant finalResultInstant = pickAt(finalResultAt);

        if (appStatus != null && currentStep == ApplicationProgressStep.BEFORE_APPLY) {
            currentStep = ApplicationProgressStep.DOC_REVIEWING;
        }

        if (appStatus != null) {
            // 서류 불합격은 "서류 결과 발표" 이후에만 고정
            if (isDocFailed(appStatus)) {
                if (docResultInstant != null && !now.isBefore(docResultInstant)) {
                    currentStep = ApplicationProgressStep.DOC_RESULT_PUBLISHED;
                }
            }
            // 최종 결과(합/불) 확정도 "최종 발표일" 이후에만 고정
            else if (isFinalDecided(appStatus)) {
                if (finalResultInstant != null && !now.isBefore(finalResultInstant)) {
                    currentStep = ApplicationProgressStep.FINAL_RESULT_PUBLISHED;
                }
            }
        }

        List<MyApplicationListInfo.ProgressStepInfo> steps = List.of(
                toStep(ApplicationProgressStep.BEFORE_APPLY, "지원 전", currentStep),
                toStep(ApplicationProgressStep.DOC_REVIEWING, "서류 평가 중", currentStep),
                toStep(ApplicationProgressStep.DOC_RESULT_PUBLISHED, "서류 결과 발표", currentStep),
                toStep(ApplicationProgressStep.INTERVIEW_WAITING, "면접 대기 중", currentStep),
                toStep(ApplicationProgressStep.FINAL_REVIEWING, "최종 평가 중", currentStep),
                toStep(ApplicationProgressStep.FINAL_RESULT_PUBLISHED, "최종 결과 발표", currentStep)
        );

        NoticeComputed notice = computeNoticeForUI(now, currentStep, applyWindow, docResultAt, finalResultAt,
                appStatus);

        MyApplicationListInfo.ProgressTimelineInfo timeline = new MyApplicationListInfo.ProgressTimelineInfo(
                currentStep.name(),
                steps,
                notice.noticeType,
                notice.noticeDate,
                notice.nextRecruitmentMonth
        );

        return new ProgressComputed(
                now,
                currentStep,
                timeline,
                (applyWindow == null) ? null : applyWindow.getEndsAt(),
                docResultInstant,
                finalResultInstant
        );
    }

    private ApplicationProgressStep computeCurrentStepForUI(
            Instant now,
            RecruitmentSchedule applyWindow,
            RecruitmentSchedule docReviewWindow,
            RecruitmentSchedule docResultAt,
            RecruitmentSchedule interviewWindow,
            RecruitmentSchedule finalReviewWindow,
            RecruitmentSchedule finalResultAt
    ) {
        Instant applyStart = (applyWindow == null) ? null : applyWindow.getStartsAt();
        Instant applyEnd = (applyWindow == null) ? null : applyWindow.getEndsAt();

        Instant docResultInstant = pickAt(docResultAt);
        Instant interviewStart = (interviewWindow == null) ? null : interviewWindow.getStartsAt();
        Instant interviewEnd = (interviewWindow == null) ? null : interviewWindow.getEndsAt();

        Instant finalResultInstant = pickAt(finalResultAt);

        // 1) 지원 전
        if (applyStart != null && now.isBefore(applyStart)) {
            return ApplicationProgressStep.BEFORE_APPLY;
        }
        if (isNowInWindow(now, applyWindow)) {
            return ApplicationProgressStep.BEFORE_APPLY;
        }

        // 2) 서류 평가 중
        if (isNowInWindow(now, docReviewWindow)) {
            return ApplicationProgressStep.DOC_REVIEWING;
        }
        if (docResultInstant != null && now.isBefore(docResultInstant)) {
            return ApplicationProgressStep.DOC_REVIEWING;
        }

        // 3) 서류 결과 발표 (docResultAt 이후 ~ 면접 시작 전)
        if (docResultInstant != null && !now.isBefore(docResultInstant)) {
            if (interviewStart == null || now.isBefore(interviewStart)) {
                return ApplicationProgressStep.DOC_RESULT_PUBLISHED;
            }
        }

        // 4) 면접 단계
        if (isNowInWindow(now, interviewWindow)) {
            return ApplicationProgressStep.INTERVIEW_WAITING;
        }

        // 5) 최종 평가 중
        if (isNowInWindow(now, finalReviewWindow)) {
            return ApplicationProgressStep.FINAL_REVIEWING;
        }
        if (finalResultInstant != null) {
            if (interviewEnd != null && now.isAfter(interviewEnd) && now.isBefore(finalResultInstant)) {
                return ApplicationProgressStep.FINAL_REVIEWING;
            }
        }

        // 6) 최종 결과 발표
        if (finalResultInstant != null && !now.isBefore(finalResultInstant)) {
            return ApplicationProgressStep.FINAL_RESULT_PUBLISHED;
        }

        // fallback
        return ApplicationProgressStep.BEFORE_APPLY;
    }

    private static record NoticeComputed(
            ApplicationProgressNoticeType noticeType,
            LocalDate noticeDate,
            Integer nextRecruitmentMonth
    ) {

        static NoticeComputed ofDate(ApplicationProgressNoticeType type, Instant instantUtc) {
            if (type == null || instantUtc == null) {
                return new NoticeComputed(null, null, null);
            }
            return new NoticeComputed(type, LocalDate.ofInstant(instantUtc, ZoneId.of("Asia/Seoul")), null);
        }

        static NoticeComputed ofNextMonth(Integer month) {
            return new NoticeComputed(ApplicationProgressNoticeType.NEXT_RECRUITMENT_EXPECTED, null, month);
        }
    }

    private NoticeComputed computeNoticeForUI(
            Instant now,
            ApplicationProgressStep step,
            RecruitmentSchedule applyWindow,
            RecruitmentSchedule docResultAt,
            RecruitmentSchedule finalResultAt,
            ApplicationStatus appStatus
    ) {
        Instant docResultInstant = pickAt(docResultAt);
        Instant finalResultInstant = pickAt(finalResultAt);

        // 최종 합격자: (최종 발표 이후에만) 앱 공지 안내 문구
        if (appStatus == ApplicationStatus.FINAL_ACCEPTED
                && step == ApplicationProgressStep.FINAL_RESULT_PUBLISHED) {
            return new NoticeComputed(ApplicationProgressNoticeType.CHALLENGER_NOTICE_IN_APP, null, null);
        }

        // 불합격자: (각 발표 이후에만) 다음 모집 월 안내
        if (appStatus != null) {
            if (isDocFailed(appStatus)) {
                if (docResultInstant != null && !now.isBefore(docResultInstant)) {
                    return NoticeComputed.ofNextMonth(computeNextRecruitmentMonth());
                }
            }
            if (isFinalRejected(appStatus)) {
                if (finalResultInstant != null && !now.isBefore(finalResultInstant)) {
                    return NoticeComputed.ofNextMonth(computeNextRecruitmentMonth());
                }
            }
        }

        // 최종 결과 발표 이후: 프론트 고정 문구 (notice null)
        if (step == ApplicationProgressStep.FINAL_RESULT_PUBLISHED) {
            return new NoticeComputed(null, null, null);
        }

        if (step == ApplicationProgressStep.BEFORE_APPLY) {
            Instant applyEnd = (applyWindow == null) ? null : applyWindow.getEndsAt();
            return NoticeComputed.ofDate(ApplicationProgressNoticeType.APPLY_DEADLINE, applyEnd);
        }

        if (step == ApplicationProgressStep.DOC_REVIEWING) {
            return NoticeComputed.ofDate(ApplicationProgressNoticeType.DOC_RESULT_ANNOUNCE, docResultInstant);
        }

        return NoticeComputed.ofDate(ApplicationProgressNoticeType.FINAL_RESULT_ANNOUNCE, finalResultInstant);
    }

    /**
     * 서류 평가는 서류 결과 발표 이후에만 decision 노출
     */
    private MyApplicationListInfo.EvaluationStatusInfo buildDocEvalForUser(
            Application app,
            ProgressComputed progress
    ) {
        // 서류 발표 전: 미정
        if (progress.docResultAt == null || progress.now.isBefore(progress.docResultAt)) {
            return new MyApplicationListInfo.EvaluationStatusInfo(EvaluationStatusCode.PENDING);
        }

        ApplicationStatus st = app.getStatus();

        if (st == ApplicationStatus.DOC_FAILED) {
            return new MyApplicationListInfo.EvaluationStatusInfo(EvaluationStatusCode.FAIL);
        }

        if (st == ApplicationStatus.DOC_PASSED
                || st == ApplicationStatus.INTERVIEW_SCHEDULED
                || st == ApplicationStatus.INTERVIEW_PASSED
                || st == ApplicationStatus.INTERVIEW_FAILED
                || st == ApplicationStatus.FINAL_ACCEPTED
                || st == ApplicationStatus.FINAL_REJECTED) {
            return new MyApplicationListInfo.EvaluationStatusInfo(EvaluationStatusCode.PASS);
        }

        // 발표 후인데 상태 반영 전
        return new MyApplicationListInfo.EvaluationStatusInfo(EvaluationStatusCode.PENDING);
    }

    /**
     * 최종 평가는 최종 결과 발표 이후에만 decision 노출 - 서류 FAIL이면 최종은 "예정 없음"
     */
    private MyApplicationListInfo.EvaluationStatusInfo buildFinalEvalForUser(
            Application app,
            ProgressComputed progress,
            MyApplicationListInfo.EvaluationStatusInfo docEval
    ) {
        // 서류 불합격이면 최종은 "예정 없음"
        if (docEval.status() == EvaluationStatusCode.FAIL) {
            return new MyApplicationListInfo.EvaluationStatusInfo(EvaluationStatusCode.NONE);
        }

        // 최종 발표 전: 미정
        if (progress.finalResultAt == null || progress.now.isBefore(progress.finalResultAt)) {
            return new MyApplicationListInfo.EvaluationStatusInfo(EvaluationStatusCode.PENDING);
        }

        ApplicationStatus st = app.getStatus();

        // 최종 발표 이후에만 합/불 확정 노출
        if (isFinalRejected(st)) {
            return new MyApplicationListInfo.EvaluationStatusInfo(EvaluationStatusCode.FAIL);
        }
        if (st == ApplicationStatus.FINAL_ACCEPTED) {
            return new MyApplicationListInfo.EvaluationStatusInfo(EvaluationStatusCode.PASS);
        }

        // 발표 후인데 최종 상태 미반영
        return new MyApplicationListInfo.EvaluationStatusInfo(EvaluationStatusCode.PENDING);
    }

    private Integer computeNextRecruitmentMonth() {
        int m = java.time.LocalDate.now().getMonthValue();
        // 상반기(1~6)면 하반기(9월), 하반기(7~12)면 다음 상반기(3월)
        return (m <= 6) ? 9 : 3;
    }


    private RecruitmentSchedule findSchedule(List<RecruitmentSchedule> schedules, RecruitmentScheduleType type) {
        return schedules.stream().filter(s -> s.getType() == type).findFirst().orElse(null);
    }

    private boolean isNowInWindow(Instant now, RecruitmentSchedule s) {
        if (s == null || s.getStartsAt() == null || s.getEndsAt() == null) {
            return false;
        }
        return !now.isBefore(s.getStartsAt()) && now.isBefore(s.getEndsAt());
    }

    private Instant pickAt(RecruitmentSchedule s) {
        if (s == null) {
            return null;
        }
        return (s.getStartsAt() != null) ? s.getStartsAt() : s.getEndsAt();
    }

    private int compareInstantDesc(Instant a, Instant b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }
        return b.compareTo(a);
    }

    private boolean isRecruitmentMatchedByFormId(Long recruitmentId, FormResponse fr) {
        if (recruitmentId == null || fr == null || fr.getForm() == null || fr.getForm().getId() == null) {
            return false;
        }
        Recruitment r = loadRecruitmentPort.findByFormId(fr.getForm().getId()).orElse(null);
        return r != null && recruitmentId.equals(r.getId());
    }

    private MyApplicationListInfo.ProgressStepInfo toStep(
            ApplicationProgressStep step,
            String label,
            ApplicationProgressStep currentStep
    ) {
        int cur = stepOrderUI(currentStep);
        int me = stepOrderUI(step);
        return new MyApplicationListInfo.ProgressStepInfo(step.name(), label, me < cur, me == cur);
    }

    private int stepOrderUI(ApplicationProgressStep step) {
        return switch (step) {
            case BEFORE_APPLY -> 1;
            case DOC_REVIEWING -> 2;
            case DOC_RESULT_PUBLISHED -> 3;
            case INTERVIEW_WAITING -> 4;
            case FINAL_REVIEWING -> 5;
            case FINAL_RESULT_PUBLISHED -> 6;
        };
    }

    private static record CurrentPicked(
            Kind kind,
            Recruitment recruitment,
            FormResponse formResponse,
            Application application
    ) {
        enum Kind {DRAFT, SUBMITTED}

        static CurrentPicked draft(Recruitment r, FormResponse fr) {
            return new CurrentPicked(Kind.DRAFT, r, fr, null);
        }

        static CurrentPicked submitted(Recruitment r, Application app) {
            return new CurrentPicked(Kind.SUBMITTED, r, null, app);
        }
    }

    private boolean isDocFailed(ApplicationStatus st) {
        return st == ApplicationStatus.DOC_FAILED;
    }

    private boolean isFinalRejected(ApplicationStatus st) {
        // 면접 불합도 최종 불합으로 취급
        return st == ApplicationStatus.INTERVIEW_FAILED || st == ApplicationStatus.FINAL_REJECTED;
    }

    private boolean isFinalDecided(ApplicationStatus st) {
        // 최종 결과(합/불) 확정 상태
        return st == ApplicationStatus.FINAL_ACCEPTED || isFinalRejected(st);
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

        // 내 지원 상태: SUBMITTED 우선 -> 없으면 DRAFT -> 없으면 NONE
        RecruitmentPartListInfo.MyApplicationInfo myApplicationInfo = resolveMyApplicationStatus(recruitment,
                query.memberId());

        var schedules = loadRecruitmentPort.findSchedulesByRecruitmentId(query.recruitmentId());
        var recruitmentPeriod = extractDatePeriod(schedules, "APPLY_WINDOW");
        //var activityPeriod = extractDatePeriod(schedules, "ACTIVITY_WINDOW");
        Gisu gisu = loadGisuPort.findById(recruitment.getGisuId());
        var activityPeriod = toDatePeriod(gisu);

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

    @Override
    public RecruitmentApplicationFormInfo get(GetRecruitmentDraftApplicationFormQuery query) {
        Recruitment recruitment = loadRecruitmentPort.findById(query.recruitmentId())
                .orElseThrow(() -> new BusinessException(
                        Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND
                ));

        // TODO: 운영진 권한 검증 필요 (requesterMemberId 기반)

        List<RecruitmentPart> parts = loadRecruitmentPartPort.findByRecruitmentId(query.recruitmentId());

        List<RecruitmentApplicationFormInfo.PreferredPartInfo.PreferredPartOptionInfo> preferredPartOptions =
                (parts == null ? List.<RecruitmentPart>of() : parts).stream()
                        .filter(p -> p.getStatus() == RecruitmentPartStatus.OPEN)
                        .map(p -> new RecruitmentApplicationFormInfo.PreferredPartInfo.PreferredPartOptionInfo(
                                p.getId(),
                                p.getPart().name(),
                                p.getPart().name()
                        ))
                        .toList();

        Integer max = recruitment.getMaxPreferredPartCount();
        if (max == null) {
            max = 1;
        }

        return loadRecruitmentPort.findApplicationFormInfoById(query.recruitmentId());
    }

    @Override
    public RecruitmentPublishedInfo get(GetPublishedRecruitmentDetailQuery query) {
        Recruitment recruitment = loadRecruitmentPort.findById(query.recruitmentId())
                .orElseThrow(
                        () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        if (recruitment.getStatus() != RecruitmentStatus.PUBLISHED) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_PUBLISHED);
        }

        // TODO: 권한 검증

        List<RecruitmentPart> recruitmentParts = loadRecruitmentPartPort.findByRecruitmentId(query.recruitmentId());
        List<ChallengerPart> parts = recruitmentParts.stream()
                .filter(RecruitmentPart::isOpen)
                .map(RecruitmentPart::getPart)
                .toList();

        RecruitmentPublishedInfo.ScheduleInfo scheduleInfo =
                loadRecruitmentPort.findPublishedScheduleInfoByRecruitmentId(query.recruitmentId());

        return RecruitmentPublishedInfo.from(recruitment, parts, scheduleInfo);
    }

    private RecruitmentPartListInfo.DatePeriod extractDatePeriod(
            List<RecruitmentSchedule> schedules,
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

    private RecruitmentPartListInfo.MyApplicationInfo resolveMyApplicationStatus(Recruitment recruitment,
                                                                                 Long memberId) {
        // 1) SUBMITTED(Application) 존재하면 SUBMITTED
        var submittedOpt = loadApplicationPort.findByRecruitmentIdAndApplicantId(recruitment.getId(), memberId);
        if (submittedOpt.isPresent()) {
            return RecruitmentPartListInfo.MyApplicationInfo.submitted(submittedOpt.get().getId());
        }

        // 2) 없으면 DRAFT(FormResponse) 찾기
        Long formId = recruitment.getFormId();
        if (formId == null) {
            return RecruitmentPartListInfo.MyApplicationInfo.none();
        }

        return loadFormResponsePort.findDraftByFormIdAndRespondentMemberId(formId, memberId)
                .map(fr -> RecruitmentPartListInfo.MyApplicationInfo.draft(fr.getId()))
                .orElseGet(RecruitmentPartListInfo.MyApplicationInfo::none);
    }

    private AnswerInfo toAnswerInfoWithPresignedUrlIfNeeded(SingleAnswer a) {
        if (a == null) {
            return null;
        }

        Map<String, Object> value = (a.getValue() == null) ? Map.of() : a.getValue();

        if (a.getAnsweredAsType() != QuestionType.PORTFOLIO) {
            return AnswerInfo.from(a);
        }

        Map<String, Object> enriched = enrichPortfolioValue(value);
        return new AnswerInfo(a.getQuestion().getId(), enriched, a.getAnsweredAsType());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> enrichPortfolioValue(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> out = new java.util.HashMap<>(raw);

        Object linksObj = out.get("links");
        if (linksObj instanceof List<?> linksList) {
            List<Map<String, Object>> normalizedLinks = new java.util.ArrayList<>();
            for (Object item : linksList) {
                if (item == null) {
                    continue;
                }

                if (item instanceof Map<?, ?> m) {
                    Object urlObj = m.get("url");
                    if (urlObj == null) {
                        urlObj = m.get("link");
                    }
                    if (urlObj == null) {
                        continue;
                    }

                    normalizedLinks.add(Map.of("url", String.valueOf(urlObj)));
                } else if (item instanceof String s) {
                    normalizedLinks.add(Map.of("url", s));
                }
            }
            out.put("links", normalizedLinks);
        }

        Object filesObj = out.get("files");

        if (filesObj == null && out.get("fileIds") instanceof List<?> fileIds) {
            List<Map<String, Object>> converted = new java.util.ArrayList<>();
            for (Object o : fileIds) {
                if (o == null) {
                    continue;
                }
                converted.add(Map.of("fileId", String.valueOf(o)));
            }
            out.remove("fileIds");
            out.put("files", converted);
            filesObj = out.get("files");
        }

        if (!(filesObj instanceof List<?> fileList)) {
            return out;
        }

        List<Map<String, Object>> enrichedFiles = new java.util.ArrayList<>();

        for (Object itemObj : fileList) {
            if (itemObj == null) {
                continue;
            }

            String fileId = null;

            if (itemObj instanceof Map<?, ?> m) {
                Object fid = (m.get("fileId") != null) ? m.get("fileId") : m.get("id");
                if (fid != null) {
                    fileId = String.valueOf(fid);
                }
            } else if (itemObj instanceof String s) {
                fileId = s;
            }

            if (fileId == null || fileId.isBlank()) {
                continue;
            }

            FileInfo info = getFileUseCase.getById(fileId);

            Map<String, Object> f = new java.util.HashMap<>();
            f.put("fileId", info.fileId());
            f.put("originalFileName", info.originalFileName());
            f.put("contentType", info.contentType());
            f.put("fileSize", info.fileSize());
            f.put("fileLink", info.fileLink());

            enrichedFiles.add(f);
        }

        out.put("files", enrichedFiles);
        return out;
    }

    private RecruitmentPublishedInfo.ScheduleInfo toPublishedScheduleInfo(
            Map<RecruitmentScheduleType, RecruitmentSchedule> map,
            Map<String, Object> interviewTimeTableRaw
    ) {
        RecruitmentSchedule apply = map == null ? null : map.get(RecruitmentScheduleType.APPLY_WINDOW);
        RecruitmentSchedule docResult = map == null ? null : map.get(RecruitmentScheduleType.DOC_RESULT_AT);
        RecruitmentSchedule interview = map == null ? null : map.get(RecruitmentScheduleType.INTERVIEW_WINDOW);
        RecruitmentSchedule finalResult = map == null ? null : map.get(RecruitmentScheduleType.FINAL_RESULT_AT);

        return new RecruitmentPublishedInfo.ScheduleInfo(
                apply == null ? null : apply.getStartsAt(),
                apply == null ? null : apply.getEndsAt(),
                docResult == null ? null : docResult.getStartsAt(),
                interview == null ? null : interview.getStartsAt(),
                interview == null ? null : interview.getEndsAt(),
                finalResult == null ? null : finalResult.getStartsAt(),
                toInterviewTimeTableInfo(interviewTimeTableRaw)
        );
    }

    @SuppressWarnings("unchecked")
    private RecruitmentPublishedInfo.InterviewTimeTableInfo toInterviewTimeTableInfo(Map<String, Object> raw) {
        if (raw == null) {
            return null;
        }

        Map<String, Object> dateRange = asMap(raw.get("dateRange"));
        Map<String, Object> timeRange = asMap(raw.get("timeRange"));

        LocalDate dateStart = parseLocalDate(dateRange.get("start"));
        LocalDate dateEnd = parseLocalDate(dateRange.get("end"));

        LocalTime timeStart = parseLocalTime(timeRange.get("start"));
        LocalTime timeEnd = parseLocalTime(timeRange.get("end"));

        Integer slotMinutes = asInteger(raw.get("slotMinutes"));

        List<RecruitmentPublishedInfo.TimesByDateInfo> enabledByDate =
                toTimesByDateInfos(raw.get("enabledByDate"));

        List<RecruitmentPublishedInfo.TimesByDateInfo> disabledByDate =
                toTimesByDateInfos(raw.get("disabledByDate"));

        return new RecruitmentPublishedInfo.InterviewTimeTableInfo(
                new RecruitmentPublishedInfo.DateRangeInfo(dateStart, dateEnd),
                new RecruitmentPublishedInfo.TimeRangeInfo(timeStart, timeEnd),
                slotMinutes,
                enabledByDate,
                disabledByDate
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object o) {
        return (o instanceof Map<?, ?> m) ? (Map<String, Object>) m : java.util.Map.of();
    }

    @SuppressWarnings("unchecked")
    private List<Object> asList(Object o) {
        return (o instanceof List<?> l) ? (List<Object>) l : java.util.List.of();
    }

    private Integer asInteger(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Integer i) {
            return i;
        }
        if (o instanceof Number n) {
            return n.intValue();
        }
        if (o instanceof String s && !s.isBlank()) {
            return Integer.parseInt(s);
        }
        return null;
    }

    private LocalDate parseLocalDate(Object o) {
        if (o == null) {
            return null;
        }
        return LocalDate.parse(o.toString()); // "YYYY-MM-DD"
    }

    private LocalTime parseLocalTime(Object o) {
        if (o == null) {
            return null;
        }
        return LocalTime.parse(o.toString()); // "HH:mm" or ISO
    }

    private List<RecruitmentPublishedInfo.TimesByDateInfo> toTimesByDateInfos(Object o) {
        List<Object> rows = asList(o);
        if (rows.isEmpty()) {
            return java.util.List.of();
        }

        return rows.stream()
                .map(this::toTimesByDateInfo)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private RecruitmentPublishedInfo.TimesByDateInfo toTimesByDateInfo(Object row) {
        Map<String, Object> m = asMap(row);

        LocalDate date = parseLocalDate(m.get("date"));

        List<Object> timesRaw = asList(m.get("times"));
        List<LocalTime> times = timesRaw.stream()
                .map(this::parseLocalTime)
                .filter(java.util.Objects::nonNull)
                .toList();

        return new RecruitmentPublishedInfo.TimesByDateInfo(date, times);
    }

    private RecruitmentPartListInfo.DatePeriod toDatePeriod(Gisu gisu) {
        if (gisu == null || gisu.getStartAt() == null || gisu.getEndAt() == null) {
            return null;
        }

        ZoneId zone = ZoneId.of("Asia/Seoul");
        return new RecruitmentPartListInfo.DatePeriod(
                gisu.getStartAt().atZone(zone).toInstant(),
                gisu.getEndAt().atZone(zone).toInstant()
        );
    }
}
