package com.umc.product.recruitment.adapter.out;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.recruitment.adapter.out.util.InterviewTimeTableDisabledCalculator;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo.ScheduleInfo;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentPublishedInfo;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormQuestionsCommand;
import com.umc.product.recruitment.application.port.in.query.RecruitmentListStatus;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo.InterviewTimeTableInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentFormDefinitionInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentListInfo;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPort;
import com.umc.product.recruitment.application.port.out.SaveRecruitmentPort;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.RecruitmentPart;
import com.umc.product.recruitment.domain.RecruitmentSchedule;
import com.umc.product.recruitment.domain.enums.RecruitmentPartStatus;
import com.umc.product.recruitment.domain.enums.RecruitmentPhase;
import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import com.umc.product.recruitment.domain.enums.RecruitmentStatus;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import com.umc.product.survey.adapter.out.persistence.FormJpaRepository;
import com.umc.product.survey.adapter.out.persistence.FormSectionJpaRepository;
import com.umc.product.survey.adapter.out.persistence.QuestionJpaRepository;
import com.umc.product.survey.adapter.out.persistence.QuestionOptionJpaRepository;
import com.umc.product.survey.application.port.in.query.dto.FormDefinitionInfo;
import com.umc.product.survey.application.port.out.LoadFormPort;
import com.umc.product.survey.domain.Form;
import com.umc.product.survey.domain.FormSection;
import com.umc.product.survey.domain.Question;
import com.umc.product.survey.domain.QuestionOption;
import com.umc.product.survey.domain.enums.FormSectionType;
import com.umc.product.survey.domain.enums.QuestionType;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecruitmentPersistenceAdapter implements SaveRecruitmentPort, LoadRecruitmentPort {
    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentPartRepository recruitmentPartRepository;
    private final RecruitmentScheduleRepository recruitmentScheduleRepository;
    private final ObjectMapper objectMapper;
    private final LoadFormPort loadFormPort;
    private final FormJpaRepository formJpaRepository;
    private final FormSectionJpaRepository formSectionJpaRepository;
    private final QuestionJpaRepository questionJpaRepository;
    private final QuestionOptionJpaRepository questionOptionJpaRepository;
    private final ApplicationRepository applicationRepository;

    @Override
    public Recruitment save(Recruitment recruitment) {
        return recruitmentRepository.save(recruitment);
    }

    @Override
    public void deleteById(Long recruitmentId) {
        recruitmentRepository.deleteById(recruitmentId);
    }

    @Override
    public Optional<Recruitment> findById(Long recruitmentId) {
        return recruitmentRepository.findById(recruitmentId);
    }

    @Override
    public RecruitmentDraftInfo findDraftInfoById(Long recruitmentId) {
        Recruitment recruitment = recruitmentRepository.findById(recruitmentId)
            .orElseThrow(
                () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        List<ChallengerPart> parts =
            recruitmentPartRepository.findByRecruitmentIdAndStatus(recruitmentId, RecruitmentPartStatus.OPEN)
                .stream()
                .map(RecruitmentPart::getPart)
                .toList();

        List<RecruitmentSchedule> schedules =
            recruitmentScheduleRepository.findByRecruitmentId(recruitmentId);

        ScheduleInfo scheduleInfo = buildScheduleInfo(
            schedules,
            recruitment.getInterviewTimeTable()
        );

        return RecruitmentDraftInfo.from(
            recruitment,
            parts,
            scheduleInfo
        );
    }

    private RecruitmentDraftInfo.ScheduleInfo buildScheduleInfo(
        List<RecruitmentSchedule> schedules,
        Map<String, Object> interviewTimeTable
    ) {

        Instant applyStart = null, applyEnd = null;
        Instant docResult = null;
        Instant interviewStart = null, interviewEnd = null;
        Instant finalResult = null;

        for (RecruitmentSchedule s : schedules) {
            if (s.getType() == null) {
                continue;
            }

            switch (s.getType()) {
                case APPLY_WINDOW -> {
                    applyStart = s.getStartsAt();
                    applyEnd = s.getEndsAt();
                }
                case DOC_RESULT_AT -> docResult = s.getStartsAt(); // AT면 startsAt만 쓰는 규칙
                case INTERVIEW_WINDOW -> {
                    interviewStart = s.getStartsAt();
                    interviewEnd = s.getEndsAt();
                }
                case FINAL_RESULT_AT -> finalResult = s.getStartsAt();
                default -> {
                }
            }
        }

        RecruitmentDraftInfo.InterviewTimeTableInfo interviewTimeTableInfo =
            parseInterviewTimeTable(interviewTimeTable);

        return new RecruitmentDraftInfo.ScheduleInfo(
            applyStart,
            applyEnd,
            docResult,
            interviewStart,
            interviewEnd,
            finalResult,
            interviewTimeTableInfo
        );
    }

    private RecruitmentDraftInfo.InterviewTimeTableInfo parseInterviewTimeTable(
        Map<String, Object> interviewTimeTable) {
        if (interviewTimeTable == null) {
            return null;
        }

        try {
            Enabled raw = objectMapper.convertValue(interviewTimeTable, Enabled.class);

            List<RecruitmentDraftInfo.TimesByDateInfo> disabledByDate =
                InterviewTimeTableDisabledCalculator.calculateDisabled(
                    raw.dateRange(),
                    raw.timeRange(),
                    raw.slotMinutes(),
                    raw.enabledByDate()
                );

            return new RecruitmentDraftInfo.InterviewTimeTableInfo(
                raw.dateRange(),
                raw.timeRange(),
                raw.slotMinutes(),
                raw.enabledByDate(),
                disabledByDate
            );
        } catch (Exception e) {
            throw new IllegalStateException("Invalid interviewTimeTable", e);
        }
    }

    private record Enabled(
        RecruitmentDraftInfo.DateRangeInfo dateRange,
        RecruitmentDraftInfo.TimeRangeInfo timeRange,
        Integer slotMinutes,
        List<RecruitmentDraftInfo.TimesByDateInfo> enabledByDate
    ) {
    }

    @Override
    public RecruitmentApplicationFormInfo findApplicationFormInfoById(Long recruitmentId) {
        Recruitment recruitment = recruitmentRepository.findById(recruitmentId)
            .orElseThrow(
                () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        Long formId = recruitment.getFormId();

        if (formId == null) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND);
        }

        FormDefinitionInfo formDefinitionInfo = loadFormPort.loadFormDefinition(formId);
        RecruitmentFormDefinitionInfo recruitmentDef = RecruitmentFormDefinitionInfo.from(formDefinitionInfo);

        List<RecruitmentPart> parts =
            recruitmentPartRepository.findByRecruitmentId(recruitmentId);

        var preferredPartInfo = buildPreferredPartInfo(recruitment, parts);

        return RecruitmentApplicationFormInfo.from(recruitment, formDefinitionInfo, recruitmentDef, null,
            preferredPartInfo);
    }

    @Override
    public void upsertQuestions(Long formId, List<UpsertRecruitmentFormQuestionsCommand.Item> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        Form form = formJpaRepository.findById(formId)
            .orElseThrow(() -> new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND));

        for (UpsertRecruitmentFormQuestionsCommand.Item item : items) {
            FormSection section = findOrCreateSection(form, item.target());
            Question question = upsertQuestionEntity(section, item.question());
            if (item.question().options() != null) {
                upsertOptions(question, item.question().options());
            }
        }
    }

    private FormSection findOrCreateSection(
        Form form,
        UpsertRecruitmentFormQuestionsCommand.Target target
    ) {
        if (target.kind() == UpsertRecruitmentFormQuestionsCommand.Target.Kind.COMMON_PAGE) {

            Integer pageNo = target.pageNo();

            return formSectionJpaRepository
                .findByFormIdAndTypeAndOrderNo(
                    form.getId(),
                    FormSectionType.DEFAULT,
                    pageNo
                )
                .orElseGet(() -> {
                    FormSection section = FormSection.builder()
                        .form(form)
                        .type(FormSectionType.DEFAULT)
                        .title("Page " + pageNo)
                        .description(null)
                        .orderNo(pageNo)
                        .targetKey(null)
                        .build();

                    return formSectionJpaRepository.save(section);
                });
        }

        // PART
        String targetKey = "PART:" + target.part().name();

        return formSectionJpaRepository
            .findByFormIdAndTypeAndTargetKey(
                form.getId(),
                FormSectionType.CUSTOM,
                targetKey
            )
            .orElseGet(() -> {
                FormSection section = FormSection.builder()
                    .form(form)
                    .type(FormSectionType.CUSTOM)
                    .title(target.part().name())
                    .description(null)
                    .orderNo(null)
                    .targetKey(targetKey)
                    .build();

                return formSectionJpaRepository.save(section);
            });
    }

    private void upsertOptions(
        Question question,
        List<UpsertRecruitmentFormQuestionsCommand.OptionInfo> options
    ) {
        for (UpsertRecruitmentFormQuestionsCommand.OptionInfo o : options) {

            QuestionOption option;

            // 수정
            if (o.optionId() != null) {
                option = questionOptionJpaRepository.findById(o.optionId())
                    .orElseThrow(() -> new IllegalStateException(
                        "Option not found. id=" + o.optionId()));
            }
            // 생성
            else {
                option = QuestionOption.builder()
                    .question(question)
                    .isOther(Boolean.TRUE.equals(o.isOther()))
                    .build();

                question.getOptions().add(option);
            }

            if (o.content() != null) {
                option.changeContent(o.content());
            }
            if (o.orderNo() != null) {
                option.changeOrderNo(o.orderNo());
            }
            if (o.isOther() != null) {
                option.changeIsOther(o.isOther());
            }

            questionOptionJpaRepository.save(option);
        }
    }

    private Question upsertQuestionEntity(FormSection section, UpsertRecruitmentFormQuestionsCommand.QuestionInfo req) {

        final Question question;

        if (req.questionId() == null) {
            // 생성
            question = Question.builder()
                .questionText(
                    req.questionText() == null ? "" : req.questionText()) // nullable 허용이면 "" 대신 기본값 정책 정해야 함
                .type(req.type() == null ? QuestionType.SHORT_TEXT : req.type())    // nullable 허용이면 기본값 정책 필요
                .isRequired(req.required() != null && req.required())
                .orderNo(req.orderNo() == null ? 0 : req.orderNo())
                .build();

            section.addQuestion(question);
            questionJpaRepository.save(question); // 도메인참조 하지 않도록 수정

            return question;
        }

        // 수정
        question = questionJpaRepository.findById(req.questionId())
            .orElseThrow(() -> new BusinessException(Domain.SURVEY, SurveyErrorCode.QUESTION_NOT_FOUND));

        if (!question.getFormSection().getId().equals(section.getId())) {
            question.getFormSection().getQuestions().remove(question);
            section.addQuestion(question);
        }

        if (req.questionText() != null) {
            question.changeQuestionText(req.questionText());
        }
        if (req.type() != null) {
            question.changeType(req.type());
        }
        if (req.required() != null) {
            question.changeRequired(req.required());
        }
        if (req.orderNo() != null) {
            question.changeOrderNo(req.orderNo());
        }

        return question;
    }

    @Override
    public boolean existsOtherOngoingPublishedRecruitment(
        Long schoolId,
        Long excludeRecruitmentId,
        Instant now
    ) {
        return recruitmentRepository.existsOtherOngoingPublishedRecruitment(
            schoolId,
            excludeRecruitmentId,
            now
        );
    }

    @Override
    public List<ChallengerPart> findPartsByRecruitmentId(Long recruitmentId) {
        return recruitmentPartRepository.findByRecruitmentId(recruitmentId)
            .stream()
            .map(RecruitmentPart::getPart)
            .toList();
    }

    @Override
    public List<RecruitmentSchedule> findSchedulesByRecruitmentId(Long recruitmentId) {
        return recruitmentScheduleRepository.findByRecruitmentId(recruitmentId);
    }

    @Override
    public List<RecruitmentListInfo.RecruitmentSummary> findRecruitmentSummaries(
        Long schoolId,
        RecruitmentListStatus status
    ) {

        List<Recruitment> recruitments = recruitmentRepository.findBySchoolIdAndStatus(schoolId,
            RecruitmentStatus.PUBLISHED);

        if (recruitments.isEmpty()) {
            return List.of();
        }

        List<Long> ids = recruitments.stream().map(Recruitment::getId).toList();

        Map<Long, RecruitmentSchedule> applyByRid = toScheduleMap(ids, RecruitmentScheduleType.APPLY_WINDOW);
        Map<Long, RecruitmentSchedule> docReviewByRid = toScheduleMap(ids, RecruitmentScheduleType.DOC_REVIEW_WINDOW);
        Map<Long, RecruitmentSchedule> docResultByRid = toScheduleMap(ids, RecruitmentScheduleType.DOC_RESULT_AT);
        Map<Long, RecruitmentSchedule> interviewByRid = toScheduleMap(ids, RecruitmentScheduleType.INTERVIEW_WINDOW);
        Map<Long, RecruitmentSchedule> finalReviewByRid = toScheduleMap(ids,
            RecruitmentScheduleType.FINAL_REVIEW_WINDOW);
        Map<Long, RecruitmentSchedule> finalResultByRid = toScheduleMap(ids, RecruitmentScheduleType.FINAL_RESULT_AT);

        Instant now = Instant.now();
        var zone = ZoneId.of("Asia/Seoul");

        return recruitments.stream()
            .map(r -> {
                Long rid = r.getId();

                RecruitmentSchedule apply = applyByRid.get(rid);
                RecruitmentSchedule docReview = docReviewByRid.get(rid);
                RecruitmentSchedule docResult = docResultByRid.get(rid);
                RecruitmentSchedule interview = interviewByRid.get(rid);
                RecruitmentSchedule finalReview = finalReviewByRid.get(rid);
                RecruitmentSchedule finalResult = finalResultByRid.get(rid);

                RecruitmentPhase phase = resolvePhase(now, apply, docReview, docResult, interview, finalReview,
                    finalResult);

                if (!matchesListStatus(status, phase)) {
                    return null;
                }

                LocalDate startDate = (apply == null || apply.getStartsAt() == null)
                    ? null
                    : apply.getStartsAt().atZone(zone).toLocalDate();

                LocalDate endDate = (finalResult == null || finalResult.getStartsAt() == null)
                    ? null
                    : finalResult.getStartsAt().atZone(zone).toLocalDate();

                int applicantCount = (int) applicationRepository.countByRecruitmentId(rid);

                boolean editable = isEditablePublished(phase); // 권한 제외 1차 로직
                // todo: 권한 도입 시 해당 editable을 덮는 수정 권한 체크 로직 추가

                return new RecruitmentListInfo.RecruitmentSummary(
                    rid,
                    r.getTitle(),
                    startDate,
                    endDate,
                    applicantCount,
                    r.getStatus(),
                    phase,
                    editable,
                    r.getUpdatedAt()
                );
            })
            .filter(x -> x != null)
            .toList();
    }

    @Override
    public List<RecruitmentListInfo.RecruitmentSummary> findDraftRecruitmentSummaries(
        Long schoolId
    ) {

        List<Recruitment> drafts = recruitmentRepository.findBySchoolIdAndStatus(schoolId, RecruitmentStatus.DRAFT);
        if (drafts.isEmpty()) {
            return List.of();
        }

        List<Long> ids = drafts.stream().map(Recruitment::getId).toList();

        Map<Long, RecruitmentSchedule> applyByRid = toScheduleMap(ids, RecruitmentScheduleType.APPLY_WINDOW);
        Map<Long, RecruitmentSchedule> docReviewByRid = toScheduleMap(ids, RecruitmentScheduleType.DOC_REVIEW_WINDOW);
        Map<Long, RecruitmentSchedule> docResultByRid = toScheduleMap(ids, RecruitmentScheduleType.DOC_RESULT_AT);
        Map<Long, RecruitmentSchedule> interviewByRid = toScheduleMap(ids, RecruitmentScheduleType.INTERVIEW_WINDOW);
        Map<Long, RecruitmentSchedule> finalReviewByRid = toScheduleMap(ids,
            RecruitmentScheduleType.FINAL_REVIEW_WINDOW);
        Map<Long, RecruitmentSchedule> finalResultByRid = toScheduleMap(ids, RecruitmentScheduleType.FINAL_RESULT_AT);

        Instant now = Instant.now();
        ZoneId zone = ZoneId.of("Asia/Seoul");

        return drafts.stream()
            .map(r -> {
                Long rid = r.getId();

                RecruitmentSchedule apply = applyByRid.get(rid);
                RecruitmentSchedule docReview = docReviewByRid.get(rid);
                RecruitmentSchedule docResult = docResultByRid.get(rid);
                RecruitmentSchedule interview = interviewByRid.get(rid);
                RecruitmentSchedule finalReview = finalReviewByRid.get(rid);
                RecruitmentSchedule finalResult = finalResultByRid.get(rid);

                RecruitmentPhase phase = resolvePhase(now, apply, docReview, docResult, interview, finalReview,
                    finalResult);

                LocalDate startDate = (apply == null || apply.getStartsAt() == null)
                    ? null
                    : apply.getStartsAt().atZone(zone).toLocalDate();

                LocalDate endDate = (finalResult == null || finalResult.getStartsAt() == null)
                    ? null
                    : finalResult.getStartsAt().atZone(zone).toLocalDate();

                int applicantCount = 0;

                return new RecruitmentListInfo.RecruitmentSummary(
                    rid,
                    r.getTitle(),
                    startDate,
                    endDate,
                    0,
                    r.getStatus(), // DRAFT
                    null,
                    true,
                    r.getUpdatedAt()
                );
            })
            .toList();
    }

    @Override
    public Optional<Long> findActiveRecruitmentId(Long schoolId, Long gisuId, Instant now) {
        return recruitmentRepository.findActiveRecruitmentId(schoolId, gisuId, now);
    }

    @Override
    public Optional<Recruitment> findByFormId(Long formId) {
        return recruitmentRepository.findByFormId(formId);
    }

    private boolean matchesListStatus(RecruitmentListStatus status, RecruitmentPhase phase) {
        return switch (status) {
            case UPCOMING -> phase == RecruitmentPhase.BEFORE_APPLY;
            case ONGOING -> phase != RecruitmentPhase.BEFORE_APPLY
                && phase != RecruitmentPhase.FINAL_RESULT_PUBLISHED
                && phase != RecruitmentPhase.CLOSED;
            case CLOSED -> phase == RecruitmentPhase.FINAL_RESULT_PUBLISHED || phase == RecruitmentPhase.CLOSED;
            case DRAFT -> false;
        };
    }

    @Override
    public RecruitmentApplicationFormInfo findApplicationFormInfoForApplicantById(
        Long recruitmentId,
        RecruitmentApplicationFormInfo.PreferredPartInfo preferredPartInfo
    ) {
        Recruitment recruitment = recruitmentRepository.findById(recruitmentId)
            .orElseThrow(
                () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        Long formId = recruitment.getFormId();
        if (formId == null) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND);
        }

        FormDefinitionInfo formDefinitionInfo = loadFormPort.loadFormDefinition(formId);
        RecruitmentFormDefinitionInfo recruitmentDef = RecruitmentFormDefinitionInfo.from(formDefinitionInfo);

        InterviewTimeTableInfo applicantTimeTable = parseInterviewTimeTableForApplicant(
            recruitment.getInterviewTimeTable());

        return RecruitmentApplicationFormInfo.from(recruitment, formDefinitionInfo, recruitmentDef, applicantTimeTable,
            preferredPartInfo);
    }

    @Override
    public List<Long> findActiveRecruitmentIds(Long schoolId, Long gisuId, Instant now) {
        return recruitmentRepository.findLatestPublishedId(schoolId, gisuId);
    }

    @Override
    public RecruitmentPublishedInfo.ScheduleInfo findPublishedScheduleInfoByRecruitmentId(Long recruitmentId) {
        Recruitment recruitment = recruitmentRepository.findById(recruitmentId)
            .orElseThrow(
                () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        List<RecruitmentSchedule> schedules =
            recruitmentScheduleRepository.findByRecruitmentId(recruitmentId);

        RecruitmentPublishedInfo.InterviewTimeTableInfo timeTable =
            parseInterviewTimeTableForPublished(recruitment.getInterviewTimeTable());

        return toPublishedScheduleInfo(schedules, timeTable);
    }

    @Override
    public Map<RecruitmentScheduleType, RecruitmentSchedule>
    findScheduleMapByRecruitmentId(Long recruitmentId) {

        return recruitmentScheduleRepository.findByRecruitmentId(recruitmentId)
            .stream()
            .collect(Collectors.toMap(
                RecruitmentSchedule::getType,
                Function.identity()
            ));
    }

    private InterviewTimeTableInfo parseInterviewTimeTableForApplicant(Map<String, Object> interviewTimeTable) {
        if (interviewTimeTable == null) {
            return null;
        }

        try {
            Enabled raw = objectMapper.convertValue(interviewTimeTable, Enabled.class);

            RecruitmentDraftInfo.DateRangeInfo dr = raw.dateRange();
            RecruitmentDraftInfo.TimeRangeInfo tr = raw.timeRange();
            Integer slotMinutes = raw.slotMinutes();
            List<RecruitmentDraftInfo.TimesByDateInfo> enabled =
                raw.enabledByDate() == null ? List.of() : raw.enabledByDate();

            InterviewTimeTableDisabledCalculator.Normalized normalized =
                InterviewTimeTableDisabledCalculator.normalizeForApplicant(dr, tr, slotMinutes, enabled);

            return toQueryInterviewTimeTableInfo(normalized);

        } catch (Exception e) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_TIMETABLE_INVALID);
        }
    }

    private InterviewTimeTableInfo toQueryInterviewTimeTableInfo(InterviewTimeTableDisabledCalculator.Normalized n) {
        if (n == null) {
            return null;
        }

        InterviewTimeTableInfo.DateRangeInfo dateRange =
            (n.dateRange() == null) ? null
                : new InterviewTimeTableInfo.DateRangeInfo(n.dateRange().start(), n.dateRange().end());

        InterviewTimeTableInfo.TimeRangeInfo timeRange =
            (n.timeRange() == null) ? null
                : new InterviewTimeTableInfo.TimeRangeInfo(n.timeRange().start(), n.timeRange().end());

        List<InterviewTimeTableInfo.TimesByDateInfo> enabled =
            (n.enabledByDate() == null) ? null
                : n.enabledByDate().stream()
                    .map(x -> new InterviewTimeTableInfo.TimesByDateInfo(x.date(), x.times()))
                    .toList();

        List<InterviewTimeTableInfo.TimesByDateInfo> disabled =
            (n.disabledByDate() == null) ? null
                : n.disabledByDate().stream()
                    .map(x -> new InterviewTimeTableInfo.TimesByDateInfo(x.date(), x.times()))
                    .toList();

        return new InterviewTimeTableInfo(
            dateRange,
            timeRange,
            n.slotMinutes(),
            enabled,
            disabled
        );
    }


    private RecruitmentPhase resolvePhase(
        Instant now,
        RecruitmentSchedule apply,
        RecruitmentSchedule docReview,
        RecruitmentSchedule docResult,
        RecruitmentSchedule interview,
        RecruitmentSchedule finalReview,
        RecruitmentSchedule finalResult
    ) {
        Instant applyStart = apply == null ? null : apply.getStartsAt();
        Instant applyEnd = apply == null ? null : apply.getEndsAt();

        Instant docReviewStart = docReview == null ? null : docReview.getStartsAt();
        Instant docReviewEnd = docReview == null ? null : docReview.getEndsAt();

        Instant docResultAt = docResult == null ? null : docResult.getStartsAt();

        Instant interviewStart = interview == null ? null : interview.getStartsAt();
        Instant interviewEnd = interview == null ? null : interview.getEndsAt();

        Instant finalReviewStart = finalReview == null ? null : finalReview.getStartsAt();
        Instant finalReviewEnd = finalReview == null ? null : finalReview.getEndsAt();

        Instant finalResultAt = finalResult == null ? null : finalResult.getStartsAt();

        if (applyStart == null || applyEnd == null || now.isBefore(applyStart)) {
            return RecruitmentPhase.BEFORE_APPLY;
        }

        if (!now.isAfter(applyEnd)) {
            return RecruitmentPhase.APPLY_OPEN;
        }

        if (isInWindow(now, docReviewStart, docReviewEnd)) {
            return RecruitmentPhase.DOC_REVIEWING;
        }

        if (docResultAt != null && !now.isBefore(docResultAt)) {
            if (interviewStart != null && now.isBefore(interviewStart)) {
                return RecruitmentPhase.DOC_RESULT_PUBLISHED;
            }
            if (isInWindow(now, interviewStart, interviewEnd)) {
                return RecruitmentPhase.INTERVIEW_WAITING;
            }
        }

        if (isInWindow(now, finalReviewStart, finalReviewEnd)) {
            return RecruitmentPhase.FINAL_REVIEWING;
        }

        if (finalResultAt != null && !now.isBefore(finalResultAt)) {
            return RecruitmentPhase.CLOSED;
        }

        return RecruitmentPhase.DOC_REVIEWING;
    }

    private boolean isInWindow(Instant now, Instant start, Instant end) {
        if (start == null || end == null) {
            return false;
        }
        return !now.isBefore(start) && now.isBefore(end);
    }

    private Map<Long, RecruitmentSchedule> toScheduleMap(List<Long> ids, RecruitmentScheduleType type) {
        return recruitmentScheduleRepository.findByRecruitmentIdInAndType(ids, type)
            .stream()
            .collect(Collectors.toMap(
                RecruitmentSchedule::getRecruitmentId,
                s -> s,
                (a, b) -> a
            ));
    }

    private RecruitmentApplicationFormInfo.PreferredPartInfo buildPreferredPartInfo(
        Recruitment recruitment,
        List<RecruitmentPart> parts
    ) {
        if (recruitment == null || parts == null) {
            return null;
        }

        int max = recruitment.getMaxPreferredPartCount() != null
            ? recruitment.getMaxPreferredPartCount()
            : 1;

        List<RecruitmentApplicationFormInfo.PreferredPartInfo.PreferredPartOptionInfo> options =
            parts.stream()
                .filter(p -> p.getStatus() == RecruitmentPartStatus.OPEN)
                .map(p -> new RecruitmentApplicationFormInfo.PreferredPartInfo.PreferredPartOptionInfo(
                    p.getId(),
                    p.getPart().name(),
                    p.getPart().name()
                ))
                .toList();

        if (options.isEmpty()) {
            return null;
        }

        return new RecruitmentApplicationFormInfo.PreferredPartInfo(max, options);
    }

    private boolean isEditablePublished(RecruitmentPhase phase) {
        if (phase == null) {
            return true;
        }
        return phase != RecruitmentPhase.CLOSED;
    }

    private RecruitmentPublishedInfo.ScheduleInfo toPublishedScheduleInfo(
        List<RecruitmentSchedule> schedules,
        RecruitmentPublishedInfo.InterviewTimeTableInfo interviewTimeTable
    ) {
        Map<RecruitmentScheduleType, RecruitmentSchedule> map =
            (schedules == null ? List.<RecruitmentSchedule>of() : schedules).stream()
                .collect(Collectors.toMap(RecruitmentSchedule::getType, Function.identity(), (a, b) -> a));

        RecruitmentSchedule apply = map.get(RecruitmentScheduleType.APPLY_WINDOW);
        RecruitmentSchedule interview = map.get(RecruitmentScheduleType.INTERVIEW_WINDOW);
        RecruitmentSchedule docResult = map.get(RecruitmentScheduleType.DOC_RESULT_AT);
        RecruitmentSchedule finalResult = map.get(RecruitmentScheduleType.FINAL_RESULT_AT);

        return new RecruitmentPublishedInfo.ScheduleInfo(
            apply == null ? null : apply.getStartsAt(),
            apply == null ? null : apply.getEndsAt(),
            docResult == null ? null : docResult.getStartsAt(),
            interview == null ? null : interview.getStartsAt(),
            interview == null ? null : interview.getEndsAt(),
            finalResult == null ? null : finalResult.getStartsAt(),
            interviewTimeTable
        );
    }

    private RecruitmentPublishedInfo.InterviewTimeTableInfo parseInterviewTimeTableForPublished(
        Map<String, Object> interviewTimeTable
    ) {
        if (interviewTimeTable == null) {
            return null;
        }

        try {
            Enabled raw = objectMapper.convertValue(interviewTimeTable, Enabled.class);

            List<RecruitmentDraftInfo.TimesByDateInfo> disabledByDate =
                InterviewTimeTableDisabledCalculator.calculateDisabled(
                    raw.dateRange(),
                    raw.timeRange(),
                    raw.slotMinutes(),
                    raw.enabledByDate()
                );

            return toPublishedInterviewTimeTableInfo(raw, disabledByDate);

        } catch (Exception e) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_TIMETABLE_INVALID);
        }
    }

    private RecruitmentPublishedInfo.InterviewTimeTableInfo toPublishedInterviewTimeTableInfo(
        Enabled raw,
        List<RecruitmentDraftInfo.TimesByDateInfo> disabledByDate
    ) {
        if (raw == null) {
            return null;
        }

        RecruitmentPublishedInfo.DateRangeInfo dateRange =
            (raw.dateRange() == null) ? null
                : new RecruitmentPublishedInfo.DateRangeInfo(raw.dateRange().start(), raw.dateRange().end());

        RecruitmentPublishedInfo.TimeRangeInfo timeRange =
            (raw.timeRange() == null) ? null
                : new RecruitmentPublishedInfo.TimeRangeInfo(raw.timeRange().start(), raw.timeRange().end());

        List<RecruitmentPublishedInfo.TimesByDateInfo> enabled =
            (raw.enabledByDate() == null) ? List.of()
                : raw.enabledByDate().stream()
                    .map(x -> new RecruitmentPublishedInfo.TimesByDateInfo(x.date(), x.times()))
                    .toList();

        List<RecruitmentPublishedInfo.TimesByDateInfo> disabled =
            (disabledByDate == null) ? List.of()
                : disabledByDate.stream()
                    .map(x -> new RecruitmentPublishedInfo.TimesByDateInfo(x.date(), x.times()))
                    .toList();

        return new RecruitmentPublishedInfo.InterviewTimeTableInfo(
            dateRange,
            timeRange,
            raw.slotMinutes(),
            enabled,
            disabled
        );
    }

}
