package com.umc.product.recruitment.application.service.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.recruitment.application.port.in.command.CreateRecruitmentDraftFormResponseUseCase;
import com.umc.product.recruitment.application.port.in.command.CreateRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteRecruitmentFormQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteRecruitmentFormResponseUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.command.PublishRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.command.SubmitRecruitmentApplicationUseCase;
import com.umc.product.recruitment.application.port.in.command.UpdateRecruitmentDraftUseCase;
import com.umc.product.recruitment.application.port.in.command.UpsertRecruitmentFormQuestionsUseCase;
import com.umc.product.recruitment.application.port.in.command.UpsertRecruitmentFormResponseAnswersUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.CreateOrGetDraftFormResponseInfo;
import com.umc.product.recruitment.application.port.in.command.dto.CreateOrGetRecruitmentDraftCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateRecruitmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateRecruitmentInfo;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentFormQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentFormResponseCommand;
import com.umc.product.recruitment.application.port.in.command.dto.PublishRecruitmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.PublishRecruitmentInfo;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import com.umc.product.recruitment.application.port.in.command.dto.SubmitRecruitmentApplicationCommand;
import com.umc.product.recruitment.application.port.in.command.dto.SubmitRecruitmentApplicationInfo;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateRecruitmentDraftCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormQuestionsCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormResponseAnswersCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormResponseAnswersInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;
import com.umc.product.recruitment.application.port.out.LoadApplicationPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentSchedulePort;
import com.umc.product.recruitment.application.port.out.SaveRecruitmentPartPort;
import com.umc.product.recruitment.application.port.out.SaveRecruitmentPort;
import com.umc.product.recruitment.application.port.out.SaveRecruitmentSchedulePort;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.RecruitmentPart;
import com.umc.product.recruitment.domain.RecruitmentSchedule;
import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import com.umc.product.survey.application.port.in.query.dto.FormDefinitionInfo;
import com.umc.product.survey.application.port.out.LoadFormPort;
import com.umc.product.survey.application.port.out.LoadQuestionPort;
import com.umc.product.survey.application.port.out.SaveFormPort;
import com.umc.product.survey.application.port.out.SaveQuestionOptionPort;
import com.umc.product.survey.application.port.out.SaveQuestionPort;
import com.umc.product.survey.domain.Form;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RecruitmentService implements CreateRecruitmentDraftFormResponseUseCase,
        UpsertRecruitmentFormResponseAnswersUseCase,
        DeleteRecruitmentFormResponseUseCase,
        SubmitRecruitmentApplicationUseCase,
        CreateRecruitmentUseCase,
        DeleteRecruitmentUseCase,
        UpdateRecruitmentDraftUseCase,
        UpsertRecruitmentFormQuestionsUseCase,
        PublishRecruitmentUseCase,
        DeleteRecruitmentFormQuestionUseCase {

    private final SaveFormPort saveFormPort;
    private final SaveRecruitmentPort saveRecruitmentPort;
    private final SaveRecruitmentPartPort saveRecruitmentPartPort;
    private final LoadRecruitmentPort loadRecruitmentPort;
    private final SaveRecruitmentSchedulePort saveRecruitmentSchedulePort;
    private final LoadRecruitmentSchedulePort loadRecruitmentSchedulePort;
    private final ObjectMapper objectMapper;
    private final LoadFormPort loadFormPort;
    private final LoadApplicationPort loadApplicationPort;
    private final SaveQuestionPort saveQuestionPort;
    private final LoadQuestionPort loadQuestionPort;
    private final SaveQuestionOptionPort saveQuestionOptionPort;

    private Long resolveSchoolId() {
        return 1L;
    }

    private Long resolveActiveGisuId(Long schoolId) {
        return 1L;
    }

    @Override
    public CreateOrGetDraftFormResponseInfo createOrGet(CreateOrGetRecruitmentDraftCommand command) {
        return null;
    }

    @Override
    public UpsertRecruitmentFormResponseAnswersInfo upsert(
            UpsertRecruitmentFormResponseAnswersCommand command) {
        return null;
    }

    @Override
    public void delete(DeleteRecruitmentFormResponseCommand command) {
    }

    @Override
    public SubmitRecruitmentApplicationInfo submit(SubmitRecruitmentApplicationCommand command) {
        return null;
    }

    @Override
    public CreateRecruitmentInfo create(CreateRecruitmentCommand command) {

        Long schoolId = resolveSchoolId();
        Long gisuId = resolveActiveGisuId(schoolId);

        Form savedForm = saveFormPort.save(Form.createDraft(command.memberId()));

        Recruitment savedRecruitment = saveRecruitmentPort.save(
                Recruitment.createDraft(
                        schoolId,
                        gisuId,
                        savedForm.getId(),
                        command.recruitmentName()
                )
        );

        if (command.parts() != null && !command.parts().isEmpty()) {
            List<RecruitmentPart> openParts = command.parts().stream()
                    .distinct()
                    .map(part -> RecruitmentPart.createOpen(savedRecruitment.getId(), part))
                    .toList();

            List<RecruitmentPart> closedParts = java.util.Arrays.stream(
                            com.umc.product.common.domain.enums.ChallengerPart.values())
                    .filter(part -> !command.parts().contains(part))
                    .map(part -> RecruitmentPart.createClosed(savedRecruitment.getId(), part))
                    .toList();

            List<RecruitmentPart> allParts = new java.util.ArrayList<>(openParts);
            allParts.addAll(closedParts);
            saveRecruitmentPartPort.saveAll(allParts);
        } else {
            List<RecruitmentPart> closedParts = java.util.Arrays.stream(
                            com.umc.product.common.domain.enums.ChallengerPart.values())
                    .map(part -> RecruitmentPart.createClosed(savedRecruitment.getId(), part))
                    .toList();

            saveRecruitmentPartPort.saveAll(closedParts);
        }

        List<RecruitmentSchedule> schedules = new ArrayList<>();
        schedules.add(RecruitmentSchedule.createDraft(savedRecruitment.getId(), RecruitmentScheduleType.APPLY_WINDOW));
        schedules.add(
                RecruitmentSchedule.createDraft(savedRecruitment.getId(), RecruitmentScheduleType.DOC_REVIEW_WINDOW));
        schedules.add(RecruitmentSchedule.createDraft(savedRecruitment.getId(), RecruitmentScheduleType.DOC_RESULT_AT));
        schedules.add(
                RecruitmentSchedule.createDraft(savedRecruitment.getId(), RecruitmentScheduleType.INTERVIEW_WINDOW));
        schedules.add(
                RecruitmentSchedule.createDraft(savedRecruitment.getId(), RecruitmentScheduleType.FINAL_REVIEW_WINDOW));
        schedules.add(
                RecruitmentSchedule.createDraft(savedRecruitment.getId(), RecruitmentScheduleType.FINAL_RESULT_AT));

        saveRecruitmentSchedulePort.saveAll(schedules);

        return CreateRecruitmentInfo.of(savedRecruitment.getId(), savedForm.getId());
    }

    @Override
    public void delete(DeleteRecruitmentCommand command) {
        Recruitment recruitment = loadRecruitmentPort.findById(command.recruitmentId())
                .orElseThrow(
                        () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        // TODO: 권한 검증

        // 우선 application 기준으로 삭제 조건 (기획에 문의)
        if (loadApplicationPort.existsByRecruitmentId(recruitment.getId())) {
            throw new BusinessException(Domain.RECRUITMENT,
                    RecruitmentErrorCode.RECRUITMENT_DELETE_FORBIDDEN_HAS_APPLICANTS);
        }

        Long formId = recruitment.getFormId();
        Long recruitmentId = recruitment.getId();

        saveRecruitmentPartPort.deleteAllByRecruitmentId(recruitmentId);
        saveRecruitmentSchedulePort.deleteAllByRecruitmentId(recruitmentId);

        saveRecruitmentPort.deleteById(recruitmentId);

        if (formId != null) {
            saveFormPort.deleteById(formId);
        }
    }

    @Override
    public RecruitmentDraftInfo update(UpdateRecruitmentDraftCommand command) {
        Recruitment recruitment = loadRecruitmentPort.findById(command.recruitmentId())
                .orElseThrow(
                        () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        if (command.title() != null) {
            recruitment.changeTitle(command.title());
        }

        if (command.noticeContent() != null) {
            recruitment.changeNoticeContent(command.noticeContent());
        }

        if (command.maxPreferredPartCount() != null) {
            recruitment.changeMaxPreferredPartCount(command.maxPreferredPartCount());
        }

        if (command.schedule() != null) {
            upsertSchedules(recruitment, command.schedule());
        }

        if (command.recruitmentParts() != null) {
            saveRecruitmentPartPort.deleteAllByRecruitmentId(command.recruitmentId());

            List<RecruitmentPart> openParts = command.recruitmentParts().stream()
                    .distinct()
                    .map(part -> RecruitmentPart.createOpen(command.recruitmentId(), part))
                    .toList();

            List<RecruitmentPart> closedParts = java.util.Arrays.stream(
                            com.umc.product.common.domain.enums.ChallengerPart.values())
                    .filter(part -> !command.recruitmentParts().contains(part))
                    .map(part -> RecruitmentPart.createClosed(command.recruitmentId(), part))
                    .toList();

            List<RecruitmentPart> allParts = new java.util.ArrayList<>(openParts);
            allParts.addAll(closedParts);
            saveRecruitmentPartPort.saveAll(allParts);
        }

        saveRecruitmentPort.save(recruitment);

        return loadRecruitmentPort.findDraftInfoById(command.recruitmentId());
    }


    private void upsertSchedules(Recruitment recruitment, UpdateRecruitmentDraftCommand.ScheduleCommand schedule) {

        Long recruitmentId = recruitment.getId();

        upsertSchedulePeriod(
                recruitmentId,
                RecruitmentScheduleType.APPLY_WINDOW,
                schedule.applyStartAt(),
                schedule.applyEndAt()
        );

        upsertSchedulePeriod(
                recruitmentId,
                RecruitmentScheduleType.DOC_RESULT_AT,
                schedule.docResultAt(),
                null
        );

        upsertSchedulePeriod(
                recruitmentId,
                RecruitmentScheduleType.INTERVIEW_WINDOW,
                schedule.interviewStartAt(),
                schedule.interviewEndAt()
        );

        upsertSchedulePeriod(
                recruitmentId,
                RecruitmentScheduleType.FINAL_RESULT_AT,
                schedule.finalResultAt(),
                null
        );

        if (schedule.interviewTimeTable() != null) {
            var enabledOnlyMap = toEnabledOnlyMap(schedule.interviewTimeTable());
            recruitment.changeInterviewTimeTable(enabledOnlyMap);
        }
    }

    private void upsertSchedulePeriod(
            Long recruitmentId,
            RecruitmentScheduleType type,
            Instant startsAt,
            Instant endsAt
    ) {
        if (startsAt == null && endsAt == null) {
            return;
        }

        RecruitmentSchedule schedule = loadRecruitmentSchedulePort
                .findByRecruitmentIdAndType(recruitmentId, type);

        if (schedule == null) {
            schedule = RecruitmentSchedule.createDraft(recruitmentId, type);
        }

        schedule.changePeriod(startsAt, endsAt);

        saveRecruitmentSchedulePort.save(schedule);
    }

    private record EnabledOnly(
            RecruitmentDraftInfo.DateRangeInfo dateRange,
            RecruitmentDraftInfo.TimeRangeInfo timeRange,
            Integer slotMinutes,
            List<RecruitmentDraftInfo.TimesByDateInfo> enabledByDate
    ) {
    }

    private Map<String, Object> toEnabledOnlyMap(UpdateRecruitmentDraftCommand.InterviewTimeTableCommand t) {
        List<RecruitmentDraftInfo.TimesByDateInfo> enabled =
                t.enabledByDate() == null ? List.of()
                        : t.enabledByDate().stream()
                                .map(e -> new RecruitmentDraftInfo.TimesByDateInfo(e.date(), e.times()))
                                .toList();

        EnabledOnly payload = new EnabledOnly(
                new RecruitmentDraftInfo.DateRangeInfo(t.dateRange().start(), t.dateRange().end()),
                new RecruitmentDraftInfo.TimeRangeInfo(t.timeRange().start(), t.timeRange().end()),
                t.slotMinutes(),
                enabled
        );

        return objectMapper.convertValue(payload,
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                });
    }


    @Override
    public RecruitmentApplicationFormInfo upsert(UpsertRecruitmentFormQuestionsCommand command) {
        Long formId = loadRecruitmentPort.findById(command.recruitmentId())
                .orElseThrow(
                        () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND))
                .getFormId();

        List<UpsertRecruitmentFormQuestionsCommand.Item> items =
                command.items() == null ? List.of() : command.items();

        saveRecruitmentPort.upsertQuestions(formId, items);

        return loadRecruitmentPort.findApplicationFormInfoById(command.recruitmentId());
    }

    @Override
    public PublishRecruitmentInfo publish(PublishRecruitmentCommand command) {

        Recruitment recruitment = loadRecruitmentPort.findById(command.recruitmentId())
                .orElseThrow(
                        () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        if (command.updateRecruitmentDraftCommand() != null) {
            update(command.updateRecruitmentDraftCommand());
        }

        if (command.upsertRecruitmentFormQuestionsCommand() != null) {
            upsert(command.upsertRecruitmentFormQuestionsCommand());
        }

        RecruitmentDraftInfo finalDraft = loadRecruitmentPort.findDraftInfoById(command.recruitmentId());
        RecruitmentApplicationFormInfo finalFormInfo = loadRecruitmentPort.findApplicationFormInfoById(
                command.recruitmentId());

        validatePublishable(finalDraft, finalFormInfo);

        boolean hasOtherOngoing = loadRecruitmentPort.existsOtherOngoingPublishedRecruitment(
                recruitment.getSchoolId(),
                recruitment.getId(),
                Instant.now()
        );

        if (hasOtherOngoing) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_PUBLISH_CONFLICT);
        }

        Recruitment latest = loadRecruitmentPort.findById(command.recruitmentId())
                .orElseThrow(
                        () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));
        latest.publish();
        saveRecruitmentPort.save(latest);

        Form form = loadFormPort.findById(latest.getFormId())
                .orElseThrow(() -> new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND));
        form.publish();
        saveFormPort.save(form);

        Recruitment published = loadRecruitmentPort.findById(latest.getId())
                .orElseThrow(
                        () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        return new PublishRecruitmentInfo(
                published.getId(),
                published.getFormId(),
                published.getStatus().name(),
                published.getUpdatedAt()
        );
    }

    private void validatePublishable(
            RecruitmentDraftInfo draft,
            RecruitmentApplicationFormInfo formInfo
    ) {
        if (draft == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_PUBLISH_VALIDATION_FAILED);
        }

        if (draft.title() == null || draft.title().isBlank()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_PUBLISH_VALIDATION_FAILED);
        }

        if (draft.recruitmentParts() == null || draft.recruitmentParts().isEmpty()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_PUBLISH_VALIDATION_FAILED);
        }

        RecruitmentDraftInfo.ScheduleInfo s = draft.schedule();
        if (s == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_PUBLISH_VALIDATION_FAILED);
        }

        requireNonNull(s.applyStartAt());
        requireNonNull(s.applyEndAt());
        if (!s.applyStartAt().isBefore(s.applyEndAt())) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_PUBLISH_VALIDATION_FAILED);
        }

        requireNonNull(s.docResultAt());

        requireNonNull(s.interviewStartAt());
        requireNonNull(s.interviewEndAt());
        if (!s.interviewStartAt().isBefore(s.interviewEndAt())) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_PUBLISH_VALIDATION_FAILED);
        }

        requireNonNull(s.finalResultAt());

        if (formInfo == null || formInfo.formDefinition() == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_PUBLISH_VALIDATION_FAILED);
        }

        FormDefinitionInfo def = formInfo.formDefinition();

        boolean hasAnyQuestion = def.sections().stream()
                .anyMatch(sec -> sec.questions() != null && !sec.questions().isEmpty());

        if (!hasAnyQuestion) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_PUBLISH_VALIDATION_FAILED);
        }
    }

    private void requireNonNull(Object v) {
        if (v == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_PUBLISH_VALIDATION_FAILED);
        }
    }

    @Override
    public RecruitmentApplicationFormInfo delete(DeleteRecruitmentFormQuestionCommand command) {

        Recruitment recruitment = loadRecruitmentPort.findById(command.recruitmentId())
                .orElseThrow(
                        () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        // TODO: 권한 검증

        Long formId = recruitment.getFormId();
        if (formId == null) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND);
        }

        Long questionId = command.questionId();

        boolean owned = loadQuestionPort.existsByIdAndFormId(questionId, formId);
        if (!owned) {
            throw new BusinessException(Domain.RECRUITMENT,
                    SurveyErrorCode.QUESTION_NOT_FOUND);
        }

        saveQuestionOptionPort.deleteAllByQuestionId(questionId);
        saveQuestionPort.deleteById(questionId);

        return loadRecruitmentPort.findApplicationFormInfoById(command.recruitmentId());
    }

}
