package com.umc.product.recruitment.application.service.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberErrorCode;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.recruitment.application.port.in.command.CreateRecruitmentDraftFormResponseUseCase;
import com.umc.product.recruitment.application.port.in.command.CreateRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteRecruitmentFormQuestionUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteRecruitmentFormResponseUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteRecruitmentQuestionOptionUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.command.PublishRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.command.ResetRecruitmentDraftFormResponseUseCase;
import com.umc.product.recruitment.application.port.in.command.SubmitRecruitmentApplicationUseCase;
import com.umc.product.recruitment.application.port.in.command.UpdatePublishedRecruitmentScheduleUseCase;
import com.umc.product.recruitment.application.port.in.command.UpdateRecruitmentDraftUseCase;
import com.umc.product.recruitment.application.port.in.command.UpdateRecruitmentInterviewPreferenceUseCase;
import com.umc.product.recruitment.application.port.in.command.UpsertRecruitmentFormQuestionsUseCase;
import com.umc.product.recruitment.application.port.in.command.UpsertRecruitmentFormResponseAnswersUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.CreateDraftFormResponseCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateDraftFormResponseInfo;
import com.umc.product.recruitment.application.port.in.command.dto.CreateRecruitmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateRecruitmentInfo;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentFormQuestionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentFormResponseCommand;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentQuestionOptionCommand;
import com.umc.product.recruitment.application.port.in.command.dto.PublishRecruitmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.PublishRecruitmentInfo;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentPublishedInfo;
import com.umc.product.recruitment.application.port.in.command.dto.ResetDraftFormResponseCommand;
import com.umc.product.recruitment.application.port.in.command.dto.SubmitRecruitmentApplicationCommand;
import com.umc.product.recruitment.application.port.in.command.dto.SubmitRecruitmentApplicationInfo;
import com.umc.product.recruitment.application.port.in.command.dto.UpdatePublishedRecruitmentScheduleCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateRecruitmentDraftCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateRecruitmentInterviewPreferenceCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateRecruitmentInterviewPreferenceInfo;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormQuestionsCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormResponseAnswersCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormResponseAnswersInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;
import com.umc.product.recruitment.application.port.out.LoadApplicationPort;
import com.umc.product.recruitment.application.port.out.LoadInterviewSlotPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPartPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentSchedulePort;
import com.umc.product.recruitment.application.port.out.SaveApplicationPartPreferencePort;
import com.umc.product.recruitment.application.port.out.SaveApplicationPort;
import com.umc.product.recruitment.application.port.out.SaveInterviewSlotPort;
import com.umc.product.recruitment.application.port.out.SaveRecruitmentPartPort;
import com.umc.product.recruitment.application.port.out.SaveRecruitmentPort;
import com.umc.product.recruitment.application.port.out.SaveRecruitmentSchedulePort;
import com.umc.product.recruitment.domain.Application;
import com.umc.product.recruitment.domain.ApplicationPartPreference;
import com.umc.product.recruitment.domain.InterviewSlot;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.RecruitmentPart;
import com.umc.product.recruitment.domain.RecruitmentSchedule;
import com.umc.product.recruitment.domain.enums.RecruitmentPartStatus;
import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import com.umc.product.recruitment.domain.enums.RecruitmentStatus;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileInfo;
import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.survey.application.port.in.query.dto.FormDefinitionInfo;
import com.umc.product.survey.application.port.out.LoadFormPort;
import com.umc.product.survey.application.port.out.LoadFormResponsePort;
import com.umc.product.survey.application.port.out.LoadQuestionOptionPort;
import com.umc.product.survey.application.port.out.LoadQuestionPort;
import com.umc.product.survey.application.port.out.SaveFormPort;
import com.umc.product.survey.application.port.out.SaveFormResponsePort;
import com.umc.product.survey.application.port.out.SaveQuestionOptionPort;
import com.umc.product.survey.application.port.out.SaveQuestionPort;
import com.umc.product.survey.application.port.out.SaveSingleAnswerPort;
import com.umc.product.survey.domain.Form;
import com.umc.product.survey.domain.FormResponse;
import com.umc.product.survey.domain.Question;
import com.umc.product.survey.domain.QuestionOption;
import com.umc.product.survey.domain.SingleAnswer;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import com.umc.product.survey.domain.enums.QuestionType;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
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
    DeleteRecruitmentFormQuestionUseCase,
    UpdateRecruitmentInterviewPreferenceUseCase,
    ResetRecruitmentDraftFormResponseUseCase,
    UpdatePublishedRecruitmentScheduleUseCase,
    DeleteRecruitmentQuestionOptionUseCase {

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
    private final LoadFormResponsePort loadFormResponsePort;
    private final SaveFormResponsePort saveFormResponsePort;
    private final SaveApplicationPort saveApplicationPort;
    private final LoadRecruitmentPartPort loadRecruitmentPartPort;
    private final SaveApplicationPartPreferencePort saveApplicationPartPreferencePort;
    private final LoadMemberPort loadMemberPort;
    private final LoadGisuPort loadGisuPort;
    private final GetFileUseCase getFileUseCase;
    private final SaveSingleAnswerPort saveSingleAnswerPort;
    private final LoadQuestionOptionPort loadQuestionOptionPort;
    private final LoadInterviewSlotPort loadInterviewSlotPort;
    private final SaveInterviewSlotPort saveInterviewSlotPort;

    private Long resolveSchoolId(Long memberId) {
        Member member = loadMemberPort.findById(memberId)
            .orElseThrow(() -> new BusinessException(Domain.MEMBER, MemberErrorCode.MEMBER_NOT_FOUND));
        return member.getSchoolId();
    }

    private Long resolveActiveGisuId() {
        return loadGisuPort.findActiveGisu().getId();
    }

    @Override
    public CreateDraftFormResponseInfo create(CreateDraftFormResponseCommand command) {
        Recruitment recruitment = loadRecruitmentPort.findById(command.recruitmentId())
            .orElseThrow(
                () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        if (!recruitment.isPublished()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_PUBLISHED);
        }

        Long formId = recruitment.getFormId();
        if (formId == null) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND);
        }

        if (loadApplicationPort.existsByRecruitmentIdAndApplicantMemberId(
            recruitment.getId(), command.memberId())) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_ALREADY_APPLIED);
        }

        boolean existsDraft = loadFormResponsePort
            .findDraftByFormIdAndRespondentMemberId(formId, command.memberId())
            .isPresent();
        if (existsDraft) {
            throw new BusinessException(
                Domain.RECRUITMENT,
                RecruitmentErrorCode.DRAFT_FORM_RESPONSE_ALREADY_EXISTS
            );
        }

        Form form = loadFormPort.findById(formId)
            .orElseThrow(() -> new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND));

        FormResponse created = saveFormResponsePort.save(
            FormResponse.createDraft(form, command.memberId())
        );

        return CreateDraftFormResponseInfo.from(
            formId,
            created.getId(),
            created.getCreatedAt()
        );
    }

    @Override
    public CreateDraftFormResponseInfo reset(ResetDraftFormResponseCommand command) {
        Recruitment recruitment = loadRecruitmentPort.findById(command.recruitmentId())
            .orElseThrow(() -> new BusinessException(
                Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        if (!recruitment.isPublished()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_PUBLISHED);
        }

        Long formId = recruitment.getFormId();
        if (formId == null) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND);
        }

        if (loadApplicationPort.existsByRecruitmentIdAndApplicantMemberId(
            recruitment.getId(), command.memberId())) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_ALREADY_APPLIED);
        }

        // 기존 draft 있으면 삭제 (없으면 그냥 새로 생성)
        loadFormResponsePort.findDraftByFormIdAndRespondentMemberId(formId, command.memberId())
            .ifPresent(fr -> saveFormResponsePort.deleteById(fr.getId()));

        Form form = loadFormPort.findById(formId)
            .orElseThrow(() -> new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND));

        FormResponse created = saveFormResponsePort.save(
            FormResponse.createDraft(form, command.memberId())
        );

        return CreateDraftFormResponseInfo.from(formId, created.getId(), created.getCreatedAt());
    }

    @Override
    public UpsertRecruitmentFormResponseAnswersInfo upsert(
        UpsertRecruitmentFormResponseAnswersCommand command) {

        Recruitment recruitment = loadRecruitmentPort.findById(command.recruitmentId())
            .orElseThrow(
                () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        if (!recruitment.isPublished()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_PUBLISHED);
        }

        Long formId = recruitment.getFormId();
        if (formId == null) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND);
        }

        FormResponse formResponse = loadFormResponsePort.findById(command.formResponseId())
            .orElseThrow(() -> new BusinessException(Domain.SURVEY, SurveyErrorCode.FORM_RESPONSE_NOT_FOUND));

        if (!formResponse.getForm().getId().equals(formId)) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_FORM_MISMATCH);
        }

        if (command.memberId() != null && !command.memberId().equals(formResponse.getRespondentMemberId())) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.FORM_RESPONSE_FORBIDDEN);
        }

        List<UpsertRecruitmentFormResponseAnswersCommand.UpsertItem> items =
            (command.items() == null) ? List.of() : command.items();

        if (items.isEmpty()) {
            return UpsertRecruitmentFormResponseAnswersInfo.of(command.formResponseId(), List.of());
        }

        List<Long> savedQuestionIds = new ArrayList<>(items.size());

        for (UpsertRecruitmentFormResponseAnswersCommand.UpsertItem item : items) {
            Long questionId = item.questionId();

            if (questionId == null) {
                throw new BusinessException(Domain.SURVEY, SurveyErrorCode.QUESTION_NOT_FOUND);
            }

            Question question = loadQuestionPort.findById(questionId)
                .orElseThrow(() -> new BusinessException(Domain.SURVEY, SurveyErrorCode.QUESTION_NOT_FOUND));

            Long questionFormId = null;
            if (question.getFormSection() != null
                && question.getFormSection().getForm() != null) {
                questionFormId = question.getFormSection().getForm().getId();
            }

            if (questionFormId == null || !questionFormId.equals(formId)) {
                throw new BusinessException(Domain.SURVEY, SurveyErrorCode.QUESTION_IS_NOT_OWNED_BY_FORM);
            }

            var serverType = question.getType();
            if (item.answeredAsType() != null && item.answeredAsType() != serverType) {
                throw new BusinessException(Domain.SURVEY, SurveyErrorCode.QUESTION_TYPE_MISMATCH);
            }

            Map<String, Object> value = (item.value() == null) ? Map.of() : item.value();

            if (serverType == QuestionType.SCHEDULE) {
                value = normalizeInterviewPreferenceToHHmm(value);
            }

            if (serverType == QuestionType.PORTFOLIO) {
                validatePortfolioAnswerValue(value);
            }

            validateOtherTextIfNeeded(question, serverType, value);
            upsertSingleAnswer(formResponse, question, serverType, value);

            savedQuestionIds.add(questionId);
        }

        formResponse.updateLastSavedAt(Instant.now());
        saveFormResponsePort.save(formResponse);

        return UpsertRecruitmentFormResponseAnswersInfo.of(command.formResponseId(), savedQuestionIds);
    }

    @Override
    public void delete(DeleteRecruitmentFormResponseCommand command) {

        Recruitment recruitment = loadRecruitmentPort.findById(command.recruitmentId())
            .orElseThrow(() -> new BusinessException(
                Domain.RECRUITMENT,
                RecruitmentErrorCode.RECRUITMENT_NOT_FOUND
            ));

        if (!recruitment.isPublished()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_PUBLISHED);
        }

        Long formId = recruitment.getFormId();
        if (formId == null) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND);
        }

        FormResponse formResponse = loadFormResponsePort.findById(command.formResponseId())
            .orElseThrow(() -> new BusinessException(
                Domain.SURVEY,
                SurveyErrorCode.FORM_RESPONSE_NOT_FOUND
            ));

        if (formResponse.getForm() == null || formResponse.getForm().getId() == null
            || !formId.equals(formResponse.getForm().getId())) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_FORM_MISMATCH);
        }

        // 제출본 삭제 방지
        if (formResponse.getStatus() == FormResponseStatus.SUBMITTED) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_ALREADY_APPLIED);
        }

        saveFormResponsePort.deleteById(formResponse.getId());
    }

    @Override
    public SubmitRecruitmentApplicationInfo submit(SubmitRecruitmentApplicationCommand command) {
        Recruitment recruitment = loadRecruitmentPort.findById(command.recruitmentId())
            .orElseThrow(
                () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        if (!recruitment.isPublished()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_PUBLISHED);
        }

        Long formId = recruitment.getFormId();
        if (formId == null) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND);
        }

        validateApplyWindow(recruitment);

        FormResponse formResponse = loadFormResponsePort.findById(command.formResponseId())
            .orElseThrow(() -> new BusinessException(Domain.SURVEY, SurveyErrorCode.FORM_RESPONSE_NOT_FOUND));

        if (!formResponse.getForm().getId().equals(formId)) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_FORM_MISMATCH);
        }

        Long applicantId = command.applicantMemberId();
        Long respondentId = formResponse.getRespondentMemberId();

        boolean applicantIdExists = applicantId != null;
        boolean isSameMember = applicantIdExists && applicantId.equals(respondentId);

        log.debug(
            "[FORM_RESPONSE_AUTH] applicantMemberId={}, respondentMemberId={}, applicantIdExists={}, isSameMember={}",
            applicantId,
            respondentId,
            applicantIdExists,
            isSameMember
        );

        if (command.applicantMemberId() != null && !command.applicantMemberId()
            .equals(formResponse.getRespondentMemberId())) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.FORM_RESPONSE_FORBIDDEN);
        }

        if (formResponse.getStatus() == FormResponseStatus.SUBMITTED) {
            var existingAppOpt = loadApplicationPort.findByRecruitmentIdAndApplicantMemberId(
                recruitment.getId(), command.applicantMemberId()
            );
            if (existingAppOpt.isPresent()) {
                return SubmitRecruitmentApplicationInfo.of(
                    recruitment.getId(),
                    formResponse.getId(),
                    existingAppOpt.get().getId(),
                    com.umc.product.survey.domain.enums.FormResponseStatus.SUBMITTED
                );
            }
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_APPLICATION_INCONSISTENT);
        }

        if (loadApplicationPort.existsByRecruitmentIdAndApplicantMemberId(recruitment.getId(),
            command.applicantMemberId())) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_ALREADY_APPLIED);
        }

        validateBeforeSubmit(recruitment, formId, formResponse);

        formResponse.submit(Instant.now(), null);
        saveFormResponsePort.save(formResponse);

        Application savedApp =
            saveApplicationPort.save(
                Application.createApplied(
                    recruitment,
                    command.applicantMemberId(),
                    formResponse.getId()
                )
            );

        persistAppliedPreferredParts(recruitment, formResponse, savedApp);

        return SubmitRecruitmentApplicationInfo.of(
            recruitment.getId(),
            formResponse.getId(),
            savedApp.getId(),
            FormResponseStatus.SUBMITTED
        );
    }

    @Override
    public CreateRecruitmentInfo create(CreateRecruitmentCommand command) {

        Long schoolId = resolveSchoolId(command.memberId());
        Long gisuId = resolveActiveGisuId();

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

        if (loadApplicationPort.existsByRecruitmentId(recruitment.getId())) {
            throw new BusinessException(Domain.RECRUITMENT,
                RecruitmentErrorCode.RECRUITMENT_DELETE_FORBIDDEN_HAS_APPLICANTS);
        }

        Long formId = recruitment.getFormId();
        Long recruitmentId = recruitment.getId();

        if (formId != null) {
            List<Long> draftIds = loadFormResponsePort.findDraftIdsByFormId(formId);

            if (!draftIds.isEmpty()) {
                saveSingleAnswerPort.deleteAllByFormResponseIds(draftIds);
                saveFormResponsePort.deleteAllByIds(draftIds);
            }
        }

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

        if (recruitment.getStatus() != RecruitmentStatus.DRAFT) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_ALREADY_PUBLISHED);
        }

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
            upsertSchedulesForDraft(recruitment, command.schedule());
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


    private void upsertSchedulesForDraft(Recruitment recruitment,
                                         UpdateRecruitmentDraftCommand.ScheduleCommand schedule) {

        Long recruitmentId = recruitment.getId();

        Map<RecruitmentScheduleType, RecruitmentSchedule> existing =
            loadRecruitmentPort.findScheduleMapByRecruitmentId(recruitmentId);

        ResolvedRecruitmentSchedule resolved =
            ResolvedRecruitmentSchedule.merge(existing, schedule);

        upsertSchedulePeriod(
            recruitmentId,
            RecruitmentScheduleType.APPLY_WINDOW,
            resolved.applyStartAt(),
            resolved.applyEndAt()
        );

        upsertSchedulePeriod(
            recruitmentId,
            RecruitmentScheduleType.DOC_RESULT_AT,
            resolved.docResultAt(),
            null
        );

        upsertSchedulePeriod(
            recruitmentId,
            RecruitmentScheduleType.INTERVIEW_WINDOW,
            resolved.interviewStartAt(),
            resolved.interviewEndAt()
        );

        upsertSchedulePeriod(
            recruitmentId,
            RecruitmentScheduleType.FINAL_RESULT_AT,
            resolved.finalResultAt(),
            null
        );

        upsertReviewWindowsForDraftResolved(recruitmentId, resolved);

        if (schedule.interviewTimeTable() != null) {
            validateTimeTableStructure(schedule.interviewTimeTable());
            var enabledOnlyMap = toEnabledOnlyMap(schedule.interviewTimeTable());
            recruitment.changeInterviewTimeTable(enabledOnlyMap);
        }
    }

    private void upsertReviewWindows(Long recruitmentId, UpdateRecruitmentDraftCommand.ScheduleCommand schedule) {

        Instant docReviewStart = schedule.applyEndAt();
        Instant docReviewEnd = schedule.docResultAt();
        if (docReviewStart != null && docReviewEnd != null && docReviewStart.isBefore(docReviewEnd)) {
            upsertSchedulePeriod(
                recruitmentId,
                RecruitmentScheduleType.DOC_REVIEW_WINDOW,
                docReviewStart,
                docReviewEnd
            );
        }

        Instant finalReviewStart = schedule.interviewEndAt();
        Instant finalReviewEnd = schedule.finalResultAt();
        if (finalReviewStart != null && finalReviewEnd != null && finalReviewStart.isBefore(finalReviewEnd)) {
            upsertSchedulePeriod(
                recruitmentId,
                RecruitmentScheduleType.FINAL_REVIEW_WINDOW,
                finalReviewStart,
                finalReviewEnd
            );
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
        List<EnabledTimesByDatePayload> enabledByDate
    ) {
    }

    private record EnabledTimesByDatePayload(
        LocalDate date,
        List<String> times
    ) {
    }


    private Map<String, Object> toEnabledOnlyMap(UpdateRecruitmentDraftCommand.InterviewTimeTableCommand t) {
        List<EnabledTimesByDatePayload> enabled =
            t.enabledByDate() == null ? List.of()
                : t.enabledByDate().stream()
                    .map(e -> new EnabledTimesByDatePayload(
                        e.date(),
                        e.times() == null ? List.of()
                            : e.times().stream()
                                .filter(java.util.Objects::nonNull)
                                .map(x -> x.format(DateTimeFormatter.ofPattern("HH:mm")))
                                .toList()
                    ))
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
        validateOtherOption(command);

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

        Member requester = loadMemberPort.findById(command.requesterMemberId())
            .orElseThrow(() -> new BusinessException(Domain.MEMBER, MemberErrorCode.MEMBER_NOT_FOUND));

        if (!recruitment.getSchoolId().equals(requester.getSchoolId())) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_FORBIDDEN);
        }

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

        RecruitmentDraftInfo syncedDraft = loadRecruitmentPort.findDraftInfoById(command.recruitmentId());

        var s = syncedDraft.schedule();
        if (s != null && s.interviewTimeTable() != null) {
            validateTimeTableStructure(s.interviewTimeTable());

            validateInterviewWindowCoversTimeTable(
                s.interviewStartAt(),
                s.interviewEndAt(),
                s.interviewTimeTable()
            );
        }
        syncReviewWindowsOnPublish(command.recruitmentId(), syncedDraft.schedule());
        validateScheduleOrderOrThrow(syncedDraft.schedule());

        Instant now = Instant.now();

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
        latest.publish(now);
        saveRecruitmentPort.save(latest);

        createInterviewSlotsOnPublish(latest);

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
            published.getPublishedAt()
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
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_PUBLISH_TITLE_REQUIRED);
        }

        if (draft.recruitmentParts() == null || draft.recruitmentParts().isEmpty()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_PUBLISH_PART_REQUIRED);
        }

        RecruitmentDraftInfo.ScheduleInfo s = draft.schedule();
        if (s == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_PUBLISH_SCHEDULE_REQUIRED);
        }

        requireNonNull(s.applyStartAt(), RecruitmentErrorCode.RECRUITMENT_PUBLISH_APPLY_START_REQUIRED);
        requireNonNull(s.applyEndAt(), RecruitmentErrorCode.RECRUITMENT_PUBLISH_APPLY_END_REQUIRED);
        requireNonNull(s.docResultAt(), RecruitmentErrorCode.RECRUITMENT_PUBLISH_DOC_RESULT_REQUIRED);
        requireNonNull(s.interviewStartAt(), RecruitmentErrorCode.RECRUITMENT_PUBLISH_INTERVIEW_START_REQUIRED);
        requireNonNull(s.interviewEndAt(), RecruitmentErrorCode.RECRUITMENT_PUBLISH_INTERVIEW_END_REQUIRED);
        requireNonNull(s.finalResultAt(), RecruitmentErrorCode.RECRUITMENT_PUBLISH_FINAL_RESULT_REQUIRED);

        validateScheduleOrderOrThrow(s);

        if (formInfo == null || formInfo.formDefinition() == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_PUBLISH_VALIDATION_FAILED);
        }

        FormDefinitionInfo def = formInfo.formDefinition();

        boolean hasAnyQuestion = def.sections().stream()
            .anyMatch(sec -> sec.questions() != null && !sec.questions().isEmpty());

        if (!hasAnyQuestion) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_PUBLISH_QUESTION_REQUIRED);
        }

        boolean hasPreferredPartQuestion = def.sections().stream()
            .filter(sec -> sec.questions() != null)
            .flatMap(sec -> sec.questions().stream())
            .anyMatch(q -> q != null && q.type() == QuestionType.PREFERRED_PART);

        if (!hasPreferredPartQuestion) {
            throw new BusinessException(
                Domain.RECRUITMENT,
                RecruitmentErrorCode.RECRUITMENT_PUBLISH_PREFERRED_PART_REQUIRED
            );
        }

        if (draft.maxPreferredPartCount() != null && draft.maxPreferredPartCount() <= 0) {
            throw new BusinessException(Domain.RECRUITMENT,
                RecruitmentErrorCode.RECRUITMENT_PUBLISH_MAX_PREFERRED_PART_INVALID);
        }
    }

    private void requireNonNull(Object v, RecruitmentErrorCode code) {
        if (v == null) {
            throw new BusinessException(Domain.RECRUITMENT, code);
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

    private void upsertSingleAnswer(
        FormResponse formResponse,
        Question question,
        com.umc.product.survey.domain.enums.QuestionType answeredAsType,
        Map<String, Object> value
    ) {
        if (question == null) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.QUESTION_NOT_FOUND);
        }

        Map<String, Object> safeValue = (value == null) ? Map.of() : value;
        Long questionId = question.getId();

        var existingOpt = formResponse.getAnswers().stream()
            .filter(a -> questionId.equals(a.getQuestion().getId()))
            .findFirst();

        if (existingOpt.isPresent()) {
            existingOpt.get().change(answeredAsType, safeValue);
            return;
        }

        formResponse.getAnswers().add(
            SingleAnswer.create(formResponse, question, answeredAsType, safeValue)
        );
    }

    @Override
    public UpdateRecruitmentInterviewPreferenceInfo update(UpdateRecruitmentInterviewPreferenceCommand command) {

        Recruitment recruitment = loadRecruitmentPort.findById(command.recruitmentId())
            .orElseThrow(
                () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        if (!recruitment.isPublished()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_PUBLISHED);
        }

        Long formId = recruitment.getFormId();
        if (formId == null) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND);
        }

        FormResponse formResponse = loadFormResponsePort.findById(command.formResponseId())
            .orElseThrow(() -> new BusinessException(Domain.SURVEY, SurveyErrorCode.FORM_RESPONSE_NOT_FOUND));

        if (!formResponse.getForm().getId().equals(formId)) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_FORM_MISMATCH);
        }

        if (command.memberId() != null && !command.memberId().equals(formResponse.getRespondentMemberId())) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.FORM_RESPONSE_FORBIDDEN);
        }

        Question scheduleQuestion = loadQuestionPort.findFirstByFormIdAndType(formId, QuestionType.SCHEDULE)
            .orElseThrow(() -> new BusinessException(Domain.SURVEY, SurveyErrorCode.QUESTION_NOT_FOUND));

        Map<String, Object> safeValue = (command.value() == null) ? Map.of() : command.value();
        Map<String, Object> normalized = normalizeInterviewPreferenceToHHmm(safeValue);

        upsertSingleAnswer(
            formResponse,
            scheduleQuestion,
            QuestionType.SCHEDULE,
            normalized
        );

        saveFormResponsePort.save(formResponse);

        return UpdateRecruitmentInterviewPreferenceInfo.of(command.formResponseId(), normalized);
    }

    @Override
    public RecruitmentPublishedInfo update(UpdatePublishedRecruitmentScheduleCommand command) {
        Recruitment recruitment = loadRecruitmentPort.findById(command.recruitmentId())
            .orElseThrow(
                () -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        if (recruitment.getStatus() != RecruitmentStatus.PUBLISHED) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_PUBLISHED);
        }

        // TODO: 권한 검증 (memberId 기반)

        Map<RecruitmentScheduleType, RecruitmentSchedule> existing =
            loadRecruitmentPort.findScheduleMapByRecruitmentId(command.recruitmentId());

        Instant now = Instant.now();
        Long recruitmentId = command.recruitmentId();

        // 수정 가능 여부 검증
        validateNoPastChange(now, existing, command.schedule());
        validateApplyStartFrozenAfterStarted(now, existing, command.schedule());
        validateApplyEndNoShortenDuringOpen(now, existing, command.schedule());
        validateInterviewNoShorten(existing, command.schedule());

        ResolvedRecruitmentSchedule resolved =
            ResolvedRecruitmentSchedule.merge(existing, command.schedule());
        validateOrdering(resolved);

        // upsert schedules
        upsertSchedule(recruitmentId, existing, RecruitmentScheduleType.APPLY_WINDOW,
            resolved.applyStartAt(), resolved.applyEndAt());

        upsertAtSchedule(recruitmentId, existing, RecruitmentScheduleType.DOC_RESULT_AT,
            resolved.docResultAt());

        upsertSchedule(recruitmentId, existing, RecruitmentScheduleType.INTERVIEW_WINDOW,
            resolved.interviewStartAt(), resolved.interviewEndAt());

        upsertAtSchedule(recruitmentId, existing, RecruitmentScheduleType.FINAL_RESULT_AT,
            resolved.finalResultAt());

        upsertReviewWindowsPublished(recruitmentId, existing, resolved);

        saveRecruitmentSchedulePort.saveAll(existing.values().stream().toList());

        List<RecruitmentPart> recruitmentParts = loadRecruitmentPartPort.findByRecruitmentId(recruitmentId);
        List<ChallengerPart> parts = recruitmentParts.stream()
            .filter(RecruitmentPart::isOpen)
            .map(RecruitmentPart::getPart)
            .toList();

        RecruitmentPublishedInfo.ScheduleInfo scheduleInfo =
            loadRecruitmentPort.findPublishedScheduleInfoByRecruitmentId(recruitmentId);

        return RecruitmentPublishedInfo.from(recruitment, parts, scheduleInfo);
    }

    @Override
    public RecruitmentApplicationFormInfo delete(DeleteRecruitmentQuestionOptionCommand command) {

        Recruitment recruitment = loadRecruitmentPort.findById(command.recruitmentId())
            .orElseThrow(() -> new BusinessException(
                Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));

        if (recruitment.getStatus() != RecruitmentStatus.DRAFT) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_ALREADY_PUBLISHED);
        }

        Long formId = recruitment.getFormId();
        if (formId == null) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND);
        }

        boolean ownedQuestion = loadQuestionPort.existsByIdAndFormId(command.questionId(), formId);
        if (!ownedQuestion) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.QUESTION_NOT_FOUND);
        }

        boolean ownedOption = loadQuestionOptionPort.existsByIdAndQuestionId(command.optionId(), command.questionId());
        if (!ownedOption) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.OPTION_NOT_IN_QUESTION);
        }

        saveQuestionOptionPort.deleteById(command.optionId());

        return loadRecruitmentPort.findApplicationFormInfoById(command.recruitmentId());
    }


    private void validateApplyWindow(Recruitment recruitment) {
        RecruitmentSchedule applyWindow = loadRecruitmentSchedulePort
            .findByRecruitmentIdAndType(recruitment.getId(), RecruitmentScheduleType.APPLY_WINDOW);

        if (applyWindow == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_APPLY_WINDOW_NOT_SET);
        }

        Instant start = applyWindow.getStartsAt();
        Instant end = applyWindow.getEndsAt();

        if (start == null || end == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_APPLY_WINDOW_NOT_SET);
        }

        // start < end 기본 검증
        if (!start.isBefore(end)) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_APPLY_WINDOW_INVALID);
        }

        Instant now = Instant.now();

        if (now.isBefore(start)) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_APPLY_NOT_STARTED);
        }

        if (!now.isBefore(end)) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_APPLY_CLOSED);
        }
    }

    private void validateBeforeSubmit(Recruitment recruitment, Long formId, FormResponse formResponse) {

        RecruitmentApplicationFormInfo formInfo = loadRecruitmentPort.findApplicationFormInfoById(recruitment.getId());
        if (formInfo == null || formInfo.formDefinition() == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND);
        }

        FormDefinitionInfo def = formInfo.formDefinition();

        Set<Long> answeredQuestionIds = formResponse.getAnswers().stream()
            .map(a -> a.getQuestion().getId())
            .collect(java.util.stream.Collectors.toSet());

        log.debug("[SUBMIT_VALIDATE] recruitmentId={}, formResponseId={}, answeredQuestionIds={}",
            recruitment.getId(), formResponse.getId(), answeredQuestionIds);

        Set<ChallengerPart> selectedParts = resolveSelectedPartsForSubmit(recruitment, formResponse);

        log.debug("[SUBMIT_VALIDATE] recruitmentId={}, formResponseId={}, selectedPartsForSubmit={}",
            recruitment.getId(), formResponse.getId(), selectedParts);

        var requiredAll = def.sections().stream()
            .flatMap(sec -> sec.questions() == null ? java.util.stream.Stream.empty() : sec.questions().stream())
            .filter(FormDefinitionInfo.QuestionInfo::isRequired)
            .map(q -> java.util.Map.of("questionId", q.questionId(), "type", q.type(), "questionText",
                q.questionText()))
            .toList();

        log.debug("[SUBMIT_VALIDATE_REQUIRED_LIST] recruitmentId={}, formResponseId={}, requiredQuestions={}",
            recruitment.getId(), formResponse.getId(), requiredAll);

        def.sections().forEach(section -> {
            if (section.questions() == null) {
                return;
            }

            String targetKey = section.targetKey();
            if (isPartSection(targetKey)) {
                ChallengerPart sectionPart = parsePartFromTargetKey(targetKey);

                if (sectionPart == null || !selectedParts.contains(sectionPart)) {
                    return;
                }
            }

            section.questions().forEach(q -> {
                if (!q.isRequired()) {
                    return;
                }

                Long qid = q.questionId();
                if (qid == null || !answeredQuestionIds.contains(qid)) {
                    logRequiredMissing(
                        "validateBeforeSubmit.requiredQuestion",
                        recruitment.getId(),
                        formResponse.getId(),
                        formId,
                        java.util.Map.of(
                            "sectionTargetKey", targetKey,
                            "questionId", qid,
                            "questionType", q.type(),
                            "questionText", q.questionText(),
                            "answeredQuestionIds", answeredQuestionIds
                        ));
                    throw new BusinessException(Domain.SURVEY, SurveyErrorCode.REQUIRED_QUESTION_NOT_ANSWERED);
                }

                if (q.type() == QuestionType.PORTFOLIO) {
                    Map<String, Object> value = findAnswerValue(formResponse, qid);
                    validatePortfolioAnswerValue(value);
                    validatePortfolioFilesUploaded(value);
                }
            });
        });

        validatePreferredPartIfNeeded(recruitment, formResponse);

        validateScheduleAnswerIfNeeded(recruitment, formResponse);
    }

    private void validatePreferredPartIfNeeded(Recruitment recruitment, FormResponse formResponse) {

        Integer max = recruitment.getMaxPreferredPartCount();
        if (max == null) {
            return;
        }

        var opt = formResponse.getAnswers().stream()
            .filter(a -> a.getAnsweredAsType() == com.umc.product.survey.domain.enums.QuestionType.PREFERRED_PART)
            .findFirst();

        if (opt.isEmpty()) {
            return;
        }

        Map<String, Object> v = opt.get().getValue();
        if (v == null) {
            v = Map.of();
        }

        List<?> selected = (List<?>) (v.containsKey("preferredParts")
            ? v.getOrDefault("preferredParts", List.of())
            : v.getOrDefault("selectedParts", List.of()));

        List<Long> selectedRecruitmentPartIds = extractPreferredRecruitmentPartIds(recruitment, formResponse);
        if (selectedRecruitmentPartIds == null || selectedRecruitmentPartIds.isEmpty()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.PREFERRED_PART_INVALID);
        }

        if (selected.isEmpty()) {
            logRequiredMissing(
                "validatePreferredPartIfNeeded.emptySelection",
                recruitment.getId(),
                formResponse.getId(),
                recruitment.getFormId(),
                java.util.Map.of(
                    "maxPreferredPartCount", recruitment.getMaxPreferredPartCount(),
                    "preferredValue", v
                )
            );
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.REQUIRED_QUESTION_NOT_ANSWERED);
        }
        if (selected.size() > max) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.PREFERRED_PART_EXCEEDS_MAX_COUNT);
        }

        if (max >= 2 && selectedRecruitmentPartIds.size() < max) {
            logRequiredMissing(
                "validatePreferredPartIfNeeded.notEnoughSelections",
                recruitment.getId(),
                formResponse.getId(),
                recruitment.getFormId(),
                java.util.Map.of(
                    "maxPreferredPartCount", max,
                    "selectedSize", selectedRecruitmentPartIds.size(),
                    "preferredValue", v
                )
            );
            throw new BusinessException(Domain.RECRUITMENT,
                RecruitmentErrorCode.PREFERRED_PART_REQUIRED_COUNT_MISMATCH);
        }

        if (selectedRecruitmentPartIds == null || selectedRecruitmentPartIds.isEmpty()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.PREFERRED_PART_INVALID);
        }

        List<RecruitmentPart> parts = loadRecruitmentPartPort.findByRecruitmentId(recruitment.getId());
        if (parts == null) {
            parts = List.of();
        }

        java.util.Set<Long> openPartIds = parts.stream()
            .filter(p -> p.getId() != null)
            .filter(p -> p.getStatus() == RecruitmentPartStatus.OPEN)
            .map(RecruitmentPart::getId)
            .collect(java.util.stream.Collectors.toSet());

        boolean hasInvalid = selectedRecruitmentPartIds.stream().anyMatch(id -> !openPartIds.contains(id));
        if (hasInvalid) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.PREFERRED_PART_INVALID);
        }
    }

    @SuppressWarnings("unchecked")
    private void validateScheduleAnswerIfNeeded(Recruitment recruitment, FormResponse formResponse) {

        Map<String, Object> tt = recruitment.getInterviewTimeTable();
        if (tt == null || tt.isEmpty()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_TIMETABLE_NOT_SET);
        }

        var scheduleAnswerOpt = formResponse.getAnswers().stream()
            .filter(a -> a.getAnsweredAsType() == QuestionType.SCHEDULE)
            .findFirst();

        if (scheduleAnswerOpt.isEmpty()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_PREFERENCE_EMPTY);
        }

        Map<String, Object> value = scheduleAnswerOpt.get().getValue();
        if (value == null) {
            value = Map.of();
        }

        List<Map<String, Object>> selected =
            (List<Map<String, Object>>) value.getOrDefault("selected", List.of());

        if (selected.isEmpty()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_PREFERENCE_EMPTY);
        }

        Map<String, Object> dateRange = (Map<String, Object>) tt.get("dateRange");
        Map<String, Object> timeRange = (Map<String, Object>) tt.get("timeRange");

        if (dateRange == null || timeRange == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_TIMETABLE_NOT_SET);
        }

        LocalDate startDate = parseDate(requireString(dateRange, "start"));
        LocalDate endDate = parseDate(requireString(dateRange, "end"));

        LocalTime startTime = parseTimeFlexible(requireString(timeRange, "start"));
        LocalTime endTime = parseTimeFlexible(requireString(timeRange, "end"));

        List<Map<String, Object>> enabledByDate =
            (List<Map<String, Object>>) tt.getOrDefault("enabledByDate", List.of());

        Map<LocalDate, java.util.Set<String>> allowed = new java.util.HashMap<>();
        for (Map<String, Object> e : enabledByDate) {
            LocalDate d = parseDate(requireString(e, "date"));
            List<String> times = (List<String>) e.getOrDefault("times", List.of());

            java.util.Set<String> normalized = new java.util.HashSet<>();
            for (String t : times) {
                normalized.add(normalizeToHHmm(t));
            }
            allowed.put(d, normalized);
        }

        for (Map<String, Object> s : selected) {
            LocalDate d = parseDate(String.valueOf(s.get("date")));

            if (d.isBefore(startDate) || d.isAfter(endDate)) {
                throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_PREFERENCE_OUT_OF_RANGE);
            }

            List<String> times = (List<String>) s.getOrDefault("times", List.of());
            if (times.isEmpty()) {
                throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_PREFERENCE_EMPTY);
            }

            java.util.Set<String> allowedTimes = allowed.get(d);
            if (allowedTimes == null || allowedTimes.isEmpty()) {
                throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_PREFERENCE_INVALID_SLOT);
            }

            for (String raw : times) {
                String hhmm = normalizeToHHmm(raw);
                LocalTime t = parseTimeFlexible(raw);

                if (t.isBefore(startTime) || !t.isBefore(endTime)) {
                    throw new BusinessException(
                        Domain.RECRUITMENT,
                        RecruitmentErrorCode.INTERVIEW_PREFERENCE_OUT_OF_RANGE
                    );
                }

                if (!allowedTimes.contains(hhmm)) {
                    throw new BusinessException(Domain.RECRUITMENT,
                        RecruitmentErrorCode.INTERVIEW_PREFERENCE_INVALID_SLOT);
                }
            }
        }
    }

    private Map<String, Object> findAnswerValue(FormResponse formResponse, Long questionId) {
        if (formResponse == null || formResponse.getAnswers() == null) {
            return Map.of();
        }

        return formResponse.getAnswers().stream()
            .filter(a -> a.getQuestion() != null && questionId.equals(a.getQuestion().getId()))
            .map(SingleAnswer::getValue)
            .findFirst()
            .orElse(Map.of());
    }

    private void validatePortfolioFilesUploaded(Map<String, Object> value) {
        List<String> fileIds = extractFileIdsFromPortfolioValue(value);
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }

        for (String fileId : fileIds) {
            if (fileId == null || fileId.isBlank()) {
                throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_ANSWER_FORMAT);
            }

            FileInfo fi = getFileUseCase.getById(fileId);
            if (fi == null) {
                throw new BusinessException(Domain.STORAGE, StorageErrorCode.FILE_NOT_FOUND);
            }
            if (fi.isUploaded() == null || !fi.isUploaded()) {
                throw new BusinessException(Domain.STORAGE, StorageErrorCode.FILE_UPLOAD_NOT_COMPLETED);
            }
        }
    }


    private static LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_PREFERENCE_INVALID_FORMAT);
        }
    }

    private static LocalTime parseTimeFlexible(String s) {
        try {
            if (s.length() == 5) {
                return LocalTime.parse(s, DateTimeFormatter.ofPattern("HH:mm"));
            }
            return LocalTime.parse(s, DateTimeFormatter.ofPattern("HH:mm:ss"));
        } catch (DateTimeParseException e) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_PREFERENCE_INVALID_FORMAT);
        }
    }

    private static String normalizeToHHmm(String s) {
        if (s == null) {
            return "";
        }
        if (s.length() >= 5) {
            return s.substring(0, 5);
        }
        return s;
    }

    private void persistAppliedPreferredParts(
        Recruitment recruitment,
        FormResponse formResponse,
        Application application
    ) {
        Integer maxPreferred = recruitment.getMaxPreferredPartCount();
        if (maxPreferred != null && maxPreferred <= 0) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.PREFERRED_PART_INVALID_MAX_COUNT);
        }

        List<RecruitmentPart> parts =
            loadRecruitmentPartPort.findByRecruitmentId(recruitment.getId());
        if (parts == null) {
            parts = List.of();
        }

        List<Long> selectedRecruitmentPartIds =
            extractPreferredRecruitmentPartIds(recruitment, formResponse);

        int max = (recruitment.getMaxPreferredPartCount() != null)
            ? recruitment.getMaxPreferredPartCount()
            : 1;

        if (selectedRecruitmentPartIds.isEmpty()) {
            logRequiredMissing(
                "persistAppliedPreferredParts.emptySelectedPartIds",
                recruitment.getId(),
                formResponse.getId(),
                recruitment.getFormId(),
                java.util.Map.of(
                    "maxPreferredPartCount", recruitment.getMaxPreferredPartCount(),
                    "answersCount", formResponse.getAnswers() == null ? 0 : formResponse.getAnswers().size()
                )
            );
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.REQUIRED_QUESTION_NOT_ANSWERED);
        }
        if (selectedRecruitmentPartIds.size() > max) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.PREFERRED_PART_EXCEEDS_MAX_COUNT);
        }

        Map<Long, RecruitmentPart> partById = parts.stream()
            .filter(p -> p.getId() != null)
            .collect(java.util.stream.Collectors.toMap(
                RecruitmentPart::getId,
                p -> p,
                (a, b) -> a
            ));

        List<RecruitmentPart> selectedParts = new java.util.ArrayList<>();
        java.util.Set<Long> dedup = new java.util.HashSet<>();

        for (Long partId : selectedRecruitmentPartIds) {
            if (partId == null) {
                continue;
            }
            if (!dedup.add(partId)) {
                continue; // 중복 제거
            }

            RecruitmentPart rp = partById.get(partId);
            if (rp == null) {
                throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.PREFERRED_PART_INVALID);
            }
            if (rp.getStatus() != RecruitmentPartStatus.OPEN) {
                throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.PREFERRED_PART_INVALID);
            }
            selectedParts.add(rp);
        }

        if (selectedParts.isEmpty()) {
            logRequiredMissing(
                "persistAppliedPreferredParts.selectedPartsEmptyAfterValidation",
                recruitment.getId(),
                formResponse.getId(),
                recruitment.getFormId(),
                java.util.Map.of(
                    "selectedRecruitmentPartIds", selectedRecruitmentPartIds,
                    "openPartIds", partById.keySet()
                )
            );
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.REQUIRED_QUESTION_NOT_ANSWERED);
        }

        List<ApplicationPartPreference> prefs = new java.util.ArrayList<>();
        for (int i = 0; i < selectedParts.size(); i++) {
            int priority = i + 1;
            prefs.add(ApplicationPartPreference.create(application, selectedParts.get(i), priority));
        }

        saveApplicationPartPreferencePort.saveAll(prefs);
    }

    @SuppressWarnings("unchecked")
    private List<Long> extractPreferredRecruitmentPartIds(
        Recruitment recruitment,
        FormResponse formResponse
    ) {
        if (formResponse == null || formResponse.getAnswers() == null) {
            return List.of();
        }

        var opt = formResponse.getAnswers().stream()
            .filter(a -> a.getAnsweredAsType() == QuestionType.PREFERRED_PART)
            .findFirst();

        if (opt.isEmpty()) {
            return List.of();
        }

        Map<String, Object> v = opt.get().getValue();
        if (v == null) {
            v = Map.of();
        }

        Object rawList =
            v.containsKey("preferredParts") ? v.get("preferredParts")
                : v.containsKey("selectedParts") ? v.get("selectedParts")
                    : v.getOrDefault("preferredPartIds", List.of());

        if (!(rawList instanceof List<?> list)) {
            return List.of();
        }

        // recruitment 기준으로 OPEN part를 name->id로 매핑
        List<RecruitmentPart> parts = loadRecruitmentPartPort.findByRecruitmentId(recruitment.getId());
        if (parts == null) {
            parts = List.of();
        }

        Map<String, Long> partIdByName = parts.stream()
            .filter(p -> p.getPart() != null && p.getId() != null)
            .collect(java.util.stream.Collectors.toMap(
                p -> p.getPart().name(),
                RecruitmentPart::getId,
                (a, b) -> a
            ));

        List<Long> result = new ArrayList<>();

        for (Object item : list) {
            // 1) 숫자면 id
            if (item instanceof Number n) {
                result.add(n.longValue());
                continue;
            }

            // 2) 문자열이면: (a) 숫자 문자열 or (b) enum name
            if (item instanceof String s) {
                try {
                    result.add(Long.parseLong(s));
                } catch (NumberFormatException ignore) {
                    Long mapped = partIdByName.get(s);
                    if (mapped == null) {
                        throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.PREFERRED_PART_INVALID);
                    }
                    result.add(mapped);

                }
                continue;
            }

            // 3) map이면 recruitmentPartId / id 읽기
            if (item instanceof Map<?, ?> m) {
                Object idObj = (m.get("recruitmentPartId") != null) ? m.get("recruitmentPartId") : m.get("id");
                if (idObj instanceof Number nn) {
                    result.add(nn.longValue());
                } else if (idObj instanceof String ss) {
                    try {
                        result.add(Long.parseLong(ss));
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
        }

        return result;
    }

    private String requireString(Map<String, Object> map, String key) {
        Object v = (map == null) ? null : map.get(key);
        if (v == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_TIMETABLE_INVALID);
        }
        if (v instanceof String s) {
            return s;
        }
        return String.valueOf(v);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> normalizeInterviewPreferenceToHHmm(Map<String, Object> input) {
        if (input == null || input.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> value = unwrapValueIfNeeded(input);
        Map<String, Object> result = new HashMap<>(value);

        Object selectedObj = result.get("selected");
        if (!(selectedObj instanceof List<?> selectedList)) {
            return result;
        }

        List<Map<String, Object>> normalizedSelected = new java.util.ArrayList<>();

        for (Object itemObj : selectedList) {
            if (!(itemObj instanceof Map<?, ?> rawItem)) {
                continue;
            }

            Map<String, Object> item = new java.util.HashMap<>();
            rawItem.forEach((k, v) -> item.put(String.valueOf(k), v));

            Object timesObj = item.get("times");
            if (timesObj instanceof List<?> timesList) {
                List<String> normalizedTimes = new java.util.ArrayList<>();
                for (Object tObj : timesList) {
                    if (tObj == null) {
                        continue;
                    }

                    String s = String.valueOf(tObj).trim();

                    s = normalizeTimePrefixHHmm(s);

                    normalizedTimes.add(s);
                }
                item.put("times", normalizedTimes);
            }

            normalizedSelected.add(item);
        }

        result.put("selected", normalizedSelected);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> unwrapValueIfNeeded(Map<String, Object> input) {
        Object inner = input.get("value");
        if (inner instanceof Map<?, ?> innerMap) {
            Map<String, Object> unwrapped = new java.util.HashMap<>();
            innerMap.forEach((k, v) -> unwrapped.put(String.valueOf(k), v));
            return unwrapped;
        }
        return input;
    }

    private String normalizeTimePrefixHHmm(String s) {
        if (s.matches("^\\d{1,2}:\\d{2}(:\\d{2}(\\.\\d+)?)?$")) {
            String[] parts = s.split(":");
            String hh = parts[0].length() == 1 ? "0" + parts[0] : parts[0];
            String mm = parts[1];
            return hh + ":" + mm;
        }

        if (s.length() >= 5) {
            return s.substring(0, 5);
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    private void validatePortfolioAnswerValue(Map<String, Object> value) {
        Map<String, Object> safe = (value == null) ? Map.of() : value;

        // files/links 둘 중 하나는 있어야 함
        List<String> fileIds = extractFileIdsFromPortfolioValue(safe);
        List<String> links = extractLinksFromPortfolioValue(safe);

        boolean hasFiles = fileIds != null && !fileIds.isEmpty();
        boolean hasLinks = links != null && !links.isEmpty();

        if (!hasFiles && !hasLinks) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_ANSWER_FORMAT);
        }

        if (hasLinks) {
            for (String url : links) {
                if (url == null || url.isBlank()) {
                    throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_ANSWER_FORMAT);
                }
                String u = url.trim().toLowerCase();
                if (!(u.startsWith("http://") || u.startsWith("https://"))) {
                    throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_ANSWER_FORMAT);
                }
            }
        }

        if (hasFiles) {
            for (String fileId : fileIds) {
                if (fileId == null || fileId.isBlank()) {
                    throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_ANSWER_FORMAT);
                }

                getFileUseCase.getById(fileId);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> extractFileIdsFromPortfolioValue(Map<String, Object> value) {
        if (value == null || value.isEmpty()) {
            return List.of();
        }

        Object filesObj = value.get("files");
        Object fileIdsObj = value.get("fileIds");

        List<String> result = new ArrayList<>();

        // 1) fileIds: ["id1","id2"]
        if (fileIdsObj instanceof List<?> list) {
            for (Object it : list) {
                if (it == null) {
                    continue;
                }
                result.add(String.valueOf(it));
            }
            return result;
        }

        // 2) files: [...]
        if (filesObj instanceof List<?> list) {
            for (Object it : list) {
                if (it == null) {
                    continue;
                }

                // files: ["id1","id2"]
                if (it instanceof String s) {
                    result.add(s);
                    continue;
                }

                // files: [{fileId:"..."}, ...]
                if (it instanceof Map<?, ?> m) {
                    Object fid = m.get("fileId");
                    if (fid != null) {
                        result.add(String.valueOf(fid));
                    }
                }
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private List<String> extractLinksFromPortfolioValue(Map<String, Object> value) {
        if (value == null || value.isEmpty()) {
            return List.of();
        }

        Object linksObj = value.get("links");
        if (!(linksObj instanceof List<?> list)) {
            return List.of();
        }

        List<String> result = new ArrayList<>();

        for (Object it : list) {
            if (it == null) {
                continue;
            }

            if (it instanceof String s) {
                result.add(s);
                continue;
            }

            if (it instanceof Map<?, ?> m) {
                Object url = m.get("url");
                if (url != null) {
                    result.add(String.valueOf(url));
                }
            }
        }

        return result;
    }

    private void validateOtherOption(UpsertRecruitmentFormQuestionsCommand command) {
        if (command.items() == null || command.items().isEmpty()) {
            return;
        }

        for (UpsertRecruitmentFormQuestionsCommand.Item item : command.items()) {
            var question = item.question();
            if (question == null || question.options() == null) {
                continue;
            }

            long otherCount = question.options().stream()
                .filter(o -> Boolean.TRUE.equals(o.isOther()))
                .count();

            if (otherCount > 1) {
                throw new BusinessException(
                    Domain.SURVEY,
                    SurveyErrorCode.OTHER_OPTION_DUPLICATED
                );
            }
        }
    }

    private void validateOtherTextIfNeeded(Question question, QuestionType type, Map<String, Object> value) {
        if (question == null || type == null) {
            return;
        }
        if (value == null) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_ANSWER_FORMAT);
        }

        if (type != QuestionType.RADIO && type != QuestionType.DROPDOWN && type != QuestionType.CHECKBOX) {
            return;
        }

        Map<Long, Boolean> isOtherByOptionId = (question.getOptions() == null ? List.<QuestionOption>of()
            : question.getOptions())
            .stream()
            .filter(o -> o != null && o.getId() != null)
            .collect(Collectors.toMap(
                QuestionOption::getId,
                o -> o.isOther(),
                (a, b) -> a
            ));

        String otherText = null;
        Object otherTextRaw = value.get("otherText");
        if (otherTextRaw != null) {
            otherText = String.valueOf(otherTextRaw).trim();
            if (otherText.isEmpty()) {
                otherText = null;
            }
        }

        if (type == QuestionType.RADIO || type == QuestionType.DROPDOWN) {
            Long selectedOptionId = asLong(value.get("selectedOptionId"));
            if (selectedOptionId == null) {
                throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_ANSWER_FORMAT);
            }

            Boolean isOther = isOtherByOptionId.get(selectedOptionId);
            if (isOther == null) {
                throw new BusinessException(Domain.SURVEY, SurveyErrorCode.OPTION_NOT_IN_QUESTION);
            }

            if (Boolean.TRUE.equals(isOther) && otherText == null) {
                throw new BusinessException(Domain.SURVEY, SurveyErrorCode.OPTION_TEXT_REQUIRED);
            }

            if (!Boolean.TRUE.equals(isOther) && otherText != null) {
                throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_ANSWER_FORMAT);
            }

            return;
        }

        // CHECKBOX
        List<Long> selectedOptionIds = asLongList(value.get("selectedOptionIds"));
        if (selectedOptionIds == null) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_ANSWER_FORMAT);
        }

        boolean hasUnknown = selectedOptionIds.stream().anyMatch(id -> !isOtherByOptionId.containsKey(id));
        if (hasUnknown) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.OPTION_NOT_IN_QUESTION);
        }

        long selectedOtherCount = selectedOptionIds.stream()
            .map(isOtherByOptionId::get)
            .filter(Boolean.TRUE::equals)
            .count();

        if (selectedOtherCount > 0 && otherText == null) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.OPTION_TEXT_REQUIRED);
        }

        if (selectedOtherCount == 0 && otherText != null) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_ANSWER_FORMAT);
        }

        if (selectedOtherCount > 1) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_ANSWER_FORMAT);
        }
    }

    private Long asLong(Object raw) {
        if (raw == null) {
            return null;
        }
        try {
            if (raw instanceof Number n) {
                return n.longValue();
            }
            return Long.parseLong(String.valueOf(raw));
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Long> asLongList(Object raw) {
        if (raw == null) {
            return null;
        }
        if (!(raw instanceof List<?> list)) {
            return null;
        }

        List<Long> result = new ArrayList<>(list.size());
        for (Object x : list) {
            Long v = asLong(x);
            if (v == null) {
                return null;
            }
            result.add(v);
        }
        return result;
    }

    // published 모집 수정 가능 검증

    private void validateNoPastChange(Instant now,
                                      Map<RecruitmentScheduleType, RecruitmentSchedule> existing,
                                      UpdatePublishedRecruitmentScheduleCommand.SchedulePatch patch) {
        if (patch.applyStartAt() != null && patch.applyStartAt().isBefore(now)) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_SCHEDULE_INVALID);
        }
        if (patch.applyEndAt() != null && patch.applyEndAt().isBefore(now)) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_SCHEDULE_INVALID);
        }
        if (patch.docResultAt() != null && patch.docResultAt().isBefore(now)) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_SCHEDULE_INVALID);
        }
        if (patch.interviewStartAt() != null && patch.interviewStartAt().isBefore(now)) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_SCHEDULE_INVALID);
        }
        if (patch.interviewEndAt() != null && patch.interviewEndAt().isBefore(now)) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_SCHEDULE_INVALID);
        }
        if (patch.finalResultAt() != null && patch.finalResultAt().isBefore(now)) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_SCHEDULE_INVALID);
        }
    }

    private void validateApplyStartFrozenAfterStarted(Instant now,
                                                      Map<RecruitmentScheduleType, RecruitmentSchedule> existing,
                                                      UpdatePublishedRecruitmentScheduleCommand.SchedulePatch patch) {
        RecruitmentSchedule apply = existing.get(RecruitmentScheduleType.APPLY_WINDOW);
        if (apply == null || apply.getStartsAt() == null) {
            return;
        }

        boolean alreadyStarted = !now.isBefore(apply.getStartsAt());
        if (alreadyStarted && patch.applyStartAt() != null && !patch.applyStartAt().equals(apply.getStartsAt())) {
            throw new BusinessException(Domain.RECRUITMENT,
                RecruitmentErrorCode.RECRUITMENT_SCHEDULE_APPLY_START_FROZEN);
        }
    }

    private void validateApplyEndNoShortenDuringOpen(Instant now,
                                                     Map<RecruitmentScheduleType, RecruitmentSchedule> existing,
                                                     UpdatePublishedRecruitmentScheduleCommand.SchedulePatch patch) {
        RecruitmentSchedule apply = existing.get(RecruitmentScheduleType.APPLY_WINDOW);
        if (apply == null || apply.getStartsAt() == null || apply.getEndsAt() == null) {
            return;
        }
        if (patch.applyEndAt() == null) {
            return;
        }

        boolean isOpen = !now.isBefore(apply.getStartsAt()) && now.isBefore(apply.getEndsAt());
        if (isOpen && patch.applyEndAt().isBefore(apply.getEndsAt())) {
            throw new BusinessException(Domain.RECRUITMENT,
                RecruitmentErrorCode.RECRUITMENT_SCHEDULE_APPLY_END_SHORTEN_FORBIDDEN);
        }
    }

    private void validateInterviewNoShorten(Map<RecruitmentScheduleType, RecruitmentSchedule> existing,
                                            UpdatePublishedRecruitmentScheduleCommand.SchedulePatch patch) {
        RecruitmentSchedule interview = existing.get(RecruitmentScheduleType.INTERVIEW_WINDOW);
        if (interview == null) {
            return;
        }

        if (!interview.canChangeStartNotAdvanced(patch.interviewStartAt())) {
            throw new BusinessException(
                Domain.RECRUITMENT,
                RecruitmentErrorCode.RECRUITMENT_SCHEDULE_INTERVIEW_ADVANCE_FORBIDDEN
            );
        }

        if (!interview.canChangeEndNotShortened(patch.interviewEndAt())) {
            throw new BusinessException(
                Domain.RECRUITMENT,
                RecruitmentErrorCode.RECRUITMENT_SCHEDULE_INTERVIEW_SHORTEN_FORBIDDEN
            );
        }
    }

    private void validateOrdering(ResolvedRecruitmentSchedule c) {
        // applyStart <= applyEnd
        if (c.applyStartAt() != null && c.applyEndAt() != null && c.applyEndAt().isBefore(c.applyStartAt())) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_SCHEDULE_INVALID_ORDER);
        }
        // applyEnd <= docResult
        if (c.applyEndAt() != null && c.docResultAt() != null && c.docResultAt().isBefore(c.applyEndAt())) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_SCHEDULE_INVALID_ORDER);
        }
        // docResult <= interviewStart
        if (c.docResultAt() != null && c.interviewStartAt() != null && c.interviewStartAt().isBefore(c.docResultAt())) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_SCHEDULE_INVALID_ORDER);
        }
        // interviewStart <= interviewEnd
        if (c.interviewStartAt() != null && c.interviewEndAt() != null && c.interviewEndAt()
            .isBefore(c.interviewStartAt())) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_SCHEDULE_INVALID_ORDER);
        }
        // interviewEnd <= finalResult
        if (c.interviewEndAt() != null && c.finalResultAt() != null && c.finalResultAt().isBefore(c.interviewEndAt())) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_SCHEDULE_INVALID_ORDER);
        }
    }

    // published 모집 수정 upsert 함수
    private void upsertSchedule(
        Long recruitmentId,
        Map<RecruitmentScheduleType, RecruitmentSchedule> existing,
        RecruitmentScheduleType type,
        Instant startsAt,
        Instant endsAt
    ) {
        if (startsAt == null && endsAt == null) {
            return;
        }

        RecruitmentSchedule s = existing.get(type);
        if (s == null) {
            s = RecruitmentSchedule.create(recruitmentId, type, startsAt, endsAt);
            existing.put(type, s);
        } else {
            s.changePeriod(startsAt, endsAt);
        }
    }

    private void upsertAtSchedule(
        Long recruitmentId,
        Map<RecruitmentScheduleType, RecruitmentSchedule> existing,
        RecruitmentScheduleType type,
        Instant at
    ) {
        if (at == null) {
            return;
        }

        RecruitmentSchedule s = existing.get(type);
        if (s == null) {
            s = RecruitmentSchedule.createAt(recruitmentId, type, at);
            existing.put(type, s);
        } else {
            s.changeAt(at);
        }
    }

    private record ResolvedRecruitmentSchedule(
        Instant applyStartAt,
        Instant applyEndAt,
        Instant docResultAt,
        Instant interviewStartAt,
        Instant interviewEndAt,
        Instant finalResultAt
    ) {

        static ResolvedRecruitmentSchedule merge(
            Map<RecruitmentScheduleType, RecruitmentSchedule> existing,
            UpdatePublishedRecruitmentScheduleCommand.SchedulePatch patch
        ) {
            RecruitmentSchedule apply = existing.get(RecruitmentScheduleType.APPLY_WINDOW);
            RecruitmentSchedule docResult = existing.get(RecruitmentScheduleType.DOC_RESULT_AT);
            RecruitmentSchedule interview = existing.get(RecruitmentScheduleType.INTERVIEW_WINDOW);
            RecruitmentSchedule finalResult = existing.get(RecruitmentScheduleType.FINAL_RESULT_AT);

            Instant applyStart =
                patch.applyStartAt() != null ? patch.applyStartAt() : (apply == null ? null : apply.getStartsAt());
            Instant applyEnd =
                patch.applyEndAt() != null ? patch.applyEndAt() : (apply == null ? null : apply.getEndsAt());

            Instant docR =
                patch.docResultAt() != null ? patch.docResultAt()
                    : (docResult == null ? null : docResult.getStartsAt());

            Instant interviewStart =
                patch.interviewStartAt() != null ? patch.interviewStartAt()
                    : (interview == null ? null : interview.getStartsAt());
            Instant interviewEnd =
                patch.interviewEndAt() != null ? patch.interviewEndAt()
                    : (interview == null ? null : interview.getEndsAt());

            Instant finalR =
                patch.finalResultAt() != null ? patch.finalResultAt()
                    : (finalResult == null ? null : finalResult.getStartsAt());

            return new ResolvedRecruitmentSchedule(applyStart, applyEnd, docR, interviewStart, interviewEnd, finalR);
        }

        static ResolvedRecruitmentSchedule merge(
            Map<RecruitmentScheduleType, RecruitmentSchedule> existing,
            UpdateRecruitmentDraftCommand.ScheduleCommand patch
        ) {
            RecruitmentSchedule apply = existing.get(RecruitmentScheduleType.APPLY_WINDOW);
            RecruitmentSchedule docResult = existing.get(RecruitmentScheduleType.DOC_RESULT_AT);
            RecruitmentSchedule interview = existing.get(RecruitmentScheduleType.INTERVIEW_WINDOW);
            RecruitmentSchedule finalResult = existing.get(RecruitmentScheduleType.FINAL_RESULT_AT);

            Instant applyStart =
                patch.applyStartAt() != null ? patch.applyStartAt() : (apply == null ? null : apply.getStartsAt());
            Instant applyEnd =
                patch.applyEndAt() != null ? patch.applyEndAt() : (apply == null ? null : apply.getEndsAt());

            Instant docR =
                patch.docResultAt() != null ? patch.docResultAt()
                    : (docResult == null ? null : docResult.getStartsAt());

            Instant interviewStart =
                patch.interviewStartAt() != null ? patch.interviewStartAt()
                    : (interview == null ? null : interview.getStartsAt());
            Instant interviewEnd =
                patch.interviewEndAt() != null ? patch.interviewEndAt()
                    : (interview == null ? null : interview.getEndsAt());

            Instant finalR =
                patch.finalResultAt() != null ? patch.finalResultAt()
                    : (finalResult == null ? null : finalResult.getStartsAt());

            return new ResolvedRecruitmentSchedule(applyStart, applyEnd, docR, interviewStart, interviewEnd, finalR);
        }
    }

    private void upsertReviewWindowsPublished(
        Long recruitmentId,
        Map<RecruitmentScheduleType, RecruitmentSchedule> existing,
        ResolvedRecruitmentSchedule c
    ) {
        // DOC_REVIEW_WINDOW: applyEnd -> docResult
        if (c.applyEndAt() != null && c.docResultAt() != null) {
            upsertSchedule(
                recruitmentId,
                existing,
                RecruitmentScheduleType.DOC_REVIEW_WINDOW,
                c.applyEndAt(),
                c.docResultAt()
            );
        }

        // FINAL_REVIEW_WINDOW: interviewEnd -> finalResult
        if (c.interviewEndAt() != null && c.finalResultAt() != null) {
            upsertSchedule(
                recruitmentId,
                existing,
                RecruitmentScheduleType.FINAL_REVIEW_WINDOW,
                c.interviewEndAt(),
                c.finalResultAt()
            );
        }
    }

    private void upsertReviewWindowsForDraftResolved(Long recruitmentId, ResolvedRecruitmentSchedule r) {
        if (r.applyEndAt() != null && r.docResultAt() != null && r.applyEndAt().isBefore(r.docResultAt())) {
            upsertSchedulePeriod(recruitmentId, RecruitmentScheduleType.DOC_REVIEW_WINDOW,
                r.applyEndAt(), r.docResultAt());
        }
        if (r.interviewEndAt() != null && r.finalResultAt() != null && r.interviewEndAt().isBefore(r.finalResultAt())) {
            upsertSchedulePeriod(recruitmentId, RecruitmentScheduleType.FINAL_REVIEW_WINDOW,
                r.interviewEndAt(), r.finalResultAt());
        }

    }

    private void syncReviewWindowsOnPublish(Long recruitmentId, RecruitmentDraftInfo.ScheduleInfo s) {
        if (s == null) {
            return;
        }

        // doc review window = applyEnd -> docResult
        if (s.applyEndAt() != null && s.docResultAt() != null && !s.applyEndAt().isAfter(s.docResultAt())) {
            upsertSchedulePeriod(
                recruitmentId,
                RecruitmentScheduleType.DOC_REVIEW_WINDOW,
                s.applyEndAt(),
                s.docResultAt()
            );
        }

        // final review window = interviewEnd -> finalResult
        if (s.interviewEndAt() != null && s.finalResultAt() != null && !s.interviewEndAt().isAfter(s.finalResultAt())) {
            upsertSchedulePeriod(
                recruitmentId,
                RecruitmentScheduleType.FINAL_REVIEW_WINDOW,
                s.interviewEndAt(),
                s.finalResultAt()
            );
        }
    }

    private void validateScheduleOrderOrThrow(RecruitmentDraftInfo.ScheduleInfo s) {
        // applyStart < applyEnd
        if (!s.applyStartAt().isBefore(s.applyEndAt())) {
            throw new BusinessException(Domain.RECRUITMENT,
                RecruitmentErrorCode.RECRUITMENT_PUBLISH_SCHEDULE_ORDER_INVALID);
        }
        // applyEnd <= docResult
        if (s.docResultAt().isBefore(s.applyEndAt())) {
            throw new BusinessException(Domain.RECRUITMENT,
                RecruitmentErrorCode.RECRUITMENT_PUBLISH_SCHEDULE_ORDER_INVALID);
        }
        // docResult <= interviewStart
        if (s.interviewStartAt().isBefore(s.docResultAt())) {
            throw new BusinessException(Domain.RECRUITMENT,
                RecruitmentErrorCode.RECRUITMENT_PUBLISH_SCHEDULE_ORDER_INVALID);
        }
        // interviewStart < interviewEnd
        if (!s.interviewStartAt().isBefore(s.interviewEndAt())) {
            throw new BusinessException(Domain.RECRUITMENT,
                RecruitmentErrorCode.RECRUITMENT_PUBLISH_SCHEDULE_ORDER_INVALID);
        }
        // interviewEnd <= finalResult
        if (s.finalResultAt().isBefore(s.interviewEndAt())) {
            throw new BusinessException(Domain.RECRUITMENT,
                RecruitmentErrorCode.RECRUITMENT_PUBLISH_SCHEDULE_ORDER_INVALID);
        }
    }

    private Set<ChallengerPart> resolveSelectedPartsForSubmit(Recruitment recruitment, FormResponse formResponse) {
        List<Long> selectedRecruitmentPartIds = extractPreferredRecruitmentPartIds(recruitment, formResponse);
        if (selectedRecruitmentPartIds == null || selectedRecruitmentPartIds.isEmpty()) {
            return Set.of();
        }

        List<RecruitmentPart> parts = loadRecruitmentPartPort.findByRecruitmentId(recruitment.getId());
        if (parts == null) {
            parts = List.of();
        }

        Map<Long, ChallengerPart> partById = parts.stream()
            .filter(p -> p.getId() != null && p.getPart() != null)
            .collect(Collectors.toMap(RecruitmentPart::getId, RecruitmentPart::getPart, (a, b) -> a));

        return selectedRecruitmentPartIds.stream()
            .map(partById::get)
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private static boolean isPartSection(String targetKey) {
        return targetKey != null && targetKey.startsWith("PART:");
    }

    private static ChallengerPart parsePartFromTargetKey(String targetKey) {
        if (!isPartSection(targetKey)) {
            return null;
        }
        String raw = targetKey.substring("PART:".length()).trim();
        if (raw.isEmpty()) {
            return null;
        }

        try {
            return ChallengerPart.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void logRequiredMissing(
        String where,
        Long recruitmentId,
        Long formResponseId,
        Long formId,
        Object extra
    ) {
        log.warn("[REQUIRED_MISSING] where={}, recruitmentId={}, formResponseId={}, formId={}, extra={}",
            where, recruitmentId, formResponseId, formId, extra);
    }

    private Map<String, Object> normalizeInterviewTimeTableToHHmm(Map<String, Object> tt) {
        if (tt == null || tt.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>(tt);

        Object enabledObj = result.get("enabledByDate");
        if (!(enabledObj instanceof List<?> enabledList)) {
            return result;
        }

        List<Map<String, Object>> normalizedEnabled = new ArrayList<>();

        for (Object itemObj : enabledList) {
            if (!(itemObj instanceof Map<?, ?> raw)) {
                continue;
            }

            Map<String, Object> item = new HashMap<>();
            raw.forEach((k, v) -> item.put(String.valueOf(k), v));

            Object timesObj = item.get("times");
            if (timesObj instanceof List<?> timesList) {
                List<String> normalizedTimes = new ArrayList<>();
                for (Object tObj : timesList) {
                    if (tObj == null) {
                        continue;
                    }

                    // LocalTime / "HH:mm" / "HH:mm:ss" 모두 처리
                    if (tObj instanceof LocalTime lt) {
                        normalizedTimes.add(lt.format(DateTimeFormatter.ofPattern("HH:mm")));
                        continue;
                    }

                    String s = String.valueOf(tObj).trim();
                    normalizedTimes.add(normalizeTimePrefixHHmm(s));
                }
                item.put("times", normalizedTimes);
            }

            normalizedEnabled.add(item);
        }

        result.put("enabledByDate", normalizedEnabled);
        return result;
    }

    private void validateInterviewWindowCoversTimeTable(
        Instant interviewStartAt,
        Instant interviewEndAt,
        RecruitmentDraftInfo.InterviewTimeTableInfo tt
    ) {
        if (interviewStartAt == null || interviewEndAt == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_PUBLISH_SCHEDULE_REQUIRED);
        }
        if (tt == null || tt.dateRange() == null || tt.timeRange() == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_TIMETABLE_INVALID);
        }

        ZoneId zone = ZoneId.of("Asia/Seoul");

        LocalDate ds = tt.dateRange().start();
        LocalDate de = tt.dateRange().end();
        LocalTime ts = tt.timeRange().start();
        LocalTime te = tt.timeRange().end();

        if (ds == null || de == null || ts == null || te == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_TIMETABLE_INVALID);
        }

        Instant ttStart = ds.atTime(ts).atZone(zone).toInstant();
        Instant ttEnd = de.atTime(te).atZone(zone).toInstant();

        if (!ttStart.isBefore(ttEnd)) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_TIMETABLE_INVALID);
        }

        boolean coversStart = !interviewStartAt.isAfter(ttStart); // interviewStartAt <= ttStart
        boolean coversEnd = !interviewEndAt.isBefore(ttEnd);      // ttEnd <= interviewEndAt

        if (!coversStart || !coversEnd) {
            throw new BusinessException(
                Domain.RECRUITMENT,
                RecruitmentErrorCode.RECRUITMENT_SCHEDULE_NOT_COVER_TIMETABLE
            );
        }
    }

    private void validateTimeTableStructure(UpdateRecruitmentDraftCommand.InterviewTimeTableCommand tt) {
        if (tt == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_TIMETABLE_INVALID);
        }
        validateTimeTableStructureCommon(
            tt.dateRange() == null ? null : tt.dateRange().start(),
            tt.dateRange() == null ? null : tt.dateRange().end(),
            tt.timeRange() == null ? null : tt.timeRange().start(),
            tt.timeRange() == null ? null : tt.timeRange().end(),
            tt.slotMinutes(),
            tt.enabledByDate() == null ? List.of() : tt.enabledByDate().stream()
                .map(e -> new EnabledTimesByDatePayload(
                    e.date(),
                    e.times() == null ? List.of()
                        : e.times().stream()
                            .filter(java.util.Objects::nonNull)
                            .map(x -> x.format(DateTimeFormatter.ofPattern("HH:mm")))
                            .toList()
                ))
                .toList()
        );
    }

    private void validateTimeTableStructure(RecruitmentDraftInfo.InterviewTimeTableInfo tt) {
        if (tt == null) {
            seeInvalidTimeTable();
        }
        List<EnabledTimesByDatePayload> enabled = tt.enabledByDate() == null ? List.of()
            : tt.enabledByDate().stream()
                .map(e -> new EnabledTimesByDatePayload(
                    e.date(),
                    e.times() == null ? List.of()
                        : e.times().stream()
                            .filter(java.util.Objects::nonNull)
                            .map(t -> t.format(DateTimeFormatter.ofPattern("HH:mm")))
                            .toList()
                ))
                .toList();

        validateTimeTableStructureCommon(
            tt.dateRange() == null ? null : tt.dateRange().start(),
            tt.dateRange() == null ? null : tt.dateRange().end(),
            tt.timeRange() == null ? null : tt.timeRange().start(),
            tt.timeRange() == null ? null : tt.timeRange().end(),
            tt.slotMinutes(),
            enabled
        );
    }

    private void seeInvalidTimeTable() {
        throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.INTERVIEW_TIMETABLE_INVALID);
    }

    private void validateTimeTableStructureCommon(
        LocalDate startDate,
        LocalDate endDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer slotMinutes,
        List<EnabledTimesByDatePayload> enabledByDate
    ) {
        if (startDate == null || endDate == null || startTime == null || endTime == null) {
            seeInvalidTimeTable();
        }

        // 날짜 범위: start <= end (같은 날 허용)
        if (endDate.isBefore(startDate)) {
            seeInvalidTimeTable();
        }

        // 시간 범위: start < end (같은 시간 금지)
        if (!startTime.isBefore(endTime)) {
            seeInvalidTimeTable();
        }

        if (slotMinutes == null || slotMinutes <= 0) {
            seeInvalidTimeTable();
        }

        if (enabledByDate == null) {
            enabledByDate = List.of();
        }

        // enabledByDate의 date는 dateRange 안에 있어야 함
        // enabledByDate.times는 timeRange 안에 있어야 함 (endTime은 exclusive)
        for (EnabledTimesByDatePayload e : enabledByDate) {
            if (e == null || e.date() == null) {
                seeInvalidTimeTable();
            }

            LocalDate d = e.date();
            if (d.isBefore(startDate) || d.isAfter(endDate)) {
                seeInvalidTimeTable();
            }

            List<String> times = (e.times() == null) ? List.of() : e.times();
            for (String raw : times) {
                if (raw == null || raw.isBlank()) {
                    seeInvalidTimeTable();
                }

                // "HH:mm" / "HH:mm:ss"
                LocalTime t = parseTimeFlexible(raw);

                if (t.isBefore(startTime) || !t.isBefore(endTime)) {
                    seeInvalidTimeTable();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void createInterviewSlotsOnPublish(Recruitment recruitment) {
        if (recruitment == null) {
            return;
        }

        Long recruitmentId = recruitment.getId();
        Map<String, Object> timeTable = recruitment.getInterviewTimeTable();

        if (timeTable == null || timeTable.isEmpty()) {
            return;
        }

        // 이미 슬롯 있으면 재생성 안함
        if (loadInterviewSlotPort.existsByRecruitmentId(recruitmentId)) {
            return;
        }

        Map<String, Object> dateRange = (Map<String, Object>) timeTable.get("dateRange");
        Map<String, Object> timeRange = (Map<String, Object>) timeTable.get("timeRange");

        if (dateRange == null || timeRange == null) {
            return;
        }

        LocalDate startDate = LocalDate.parse(String.valueOf(dateRange.get("start")));
        LocalDate endDate = LocalDate.parse(String.valueOf(dateRange.get("end")));

        LocalTime startTime = LocalTime.parse(String.valueOf(timeRange.get("start")));
        LocalTime endTime = LocalTime.parse(String.valueOf(timeRange.get("end")));

        int slotMinutes = parseSlotMinutes(timeTable.get("slotMinutes"));
        if (slotMinutes <= 0) {
            throw new BusinessException(
                    Domain.RECRUITMENT,
                    RecruitmentErrorCode.INTERVIEW_TIMETABLE_INVALID
            );
        }

        Map<LocalDate, List<LocalTime>> enabledByDate = new HashMap<>();
        Object enabledRaw = timeTable.get("enabledByDate");
        if (enabledRaw instanceof List<?> enabledList) {
            for (Object itemObj : enabledList) {
                if (!(itemObj instanceof Map<?, ?> item)) {
                    continue;
                }

                Object dateObj = item.get("date");
                if (dateObj == null) {
                    continue;
                }
                LocalDate date = LocalDate.parse(String.valueOf(dateObj));

                Object timesRaw = item.get("times");
                if (!(timesRaw instanceof List<?> timesList)) {
                    continue;
                }

                List<LocalTime> times = timesList.stream()
                        .filter(java.util.Objects::nonNull)
                        .map(String::valueOf)
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .map(LocalTime::parse)
                        .distinct()
                        .sorted()
                        .toList();

                if (times.isEmpty()) {
                    // 해당 날짜 enabled 0개 -> continue
                    continue;
                }

                // 같은 날짜가 여러 번 들어오면 merge (중복 제거 + 정렬 유지)
                enabledByDate.merge(date, times, (a, b) -> {
                    return java.util.stream.Stream.concat(a.stream(), b.stream())
                            .distinct()
                            .sorted()
                            .toList();
                });
            }
        }

        // enabledByDate가 비어있으면 슬롯 생성 안함
        if (enabledByDate.isEmpty()) {
            return;
        }

        List<InterviewSlot> slots = new ArrayList<>();

        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {

            // dateRange 안에 있어도 enabledByDate에 없으면 생성 안함
            List<LocalTime> enabledTimes = enabledByDate.get(d);
            if (enabledTimes == null || enabledTimes.isEmpty()) {
                continue;
            }

            for (LocalTime t : enabledTimes) {
                // timeRange 밖은 skip
                if (t.isBefore(startTime) || t.plusMinutes(slotMinutes).isAfter(endTime)) {
                    continue;
                }
                slots.add(buildSlot(recruitment, d, t, slotMinutes));
            }
        }

        if (slots.isEmpty()) {
            return;
        }

        saveInterviewSlotPort.saveAll(slots);
    }

    private InterviewSlot buildSlot(Recruitment recruitment, LocalDate date, LocalTime startTime, int slotMinutes) {
        Instant startsAt = ZonedDateTime.of(date, startTime, ZoneId.of("Asia/Seoul")).toInstant();
        Instant endsAt = ZonedDateTime.of(date, startTime.plusMinutes(slotMinutes), ZoneId.of("Asia/Seoul"))
                .toInstant();

        return InterviewSlot.builder()
                .recruitment(recruitment)
                .startsAt(startsAt)
                .endsAt(endsAt)
                .build();
    }

    private int parseSlotMinutes(Object raw) {
        if (raw == null) {
            throw new BusinessException(
                    Domain.RECRUITMENT,
                    RecruitmentErrorCode.INTERVIEW_TIMETABLE_INVALID
            );
        }

        try {
            if (raw instanceof Number n) {
                return n.intValue();
            }

            if (raw instanceof String s) {
                String trimmed = s.trim();
                if (trimmed.isEmpty()) {
                    throw new NumberFormatException();
                }
                return Integer.parseInt(trimmed);
            }

            throw new NumberFormatException();

        } catch (NumberFormatException e) {
            throw new BusinessException(
                    Domain.RECRUITMENT,
                    RecruitmentErrorCode.INTERVIEW_TIMETABLE_INVALID
            );
        }
    }

}
