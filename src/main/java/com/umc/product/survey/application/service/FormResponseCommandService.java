package com.umc.product.survey.application.service;

import com.umc.product.survey.application.port.in.command.ManageFormResponseUseCase;
import com.umc.product.survey.application.port.in.command.dto.*;
import com.umc.product.survey.application.port.out.*;
import com.umc.product.survey.domain.*;
import com.umc.product.survey.domain.enums.QuestionType;
import com.umc.product.survey.domain.exception.SurveyDomainException;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class FormResponseCommandService implements ManageFormResponseUseCase {

    private final LoadFormPort loadFormPort;
    private final LoadQuestionPort loadQuestionPort;
    private final LoadQuestionOptionPort loadQuestionOptionPort;
    private final LoadFormResponsePort loadFormResponsePort;
    private final SaveFormResponsePort saveFormResponsePort;
    private final SaveAnswerPort saveAnswerPort;

    @Override
    public Long submitImmediately(SubmitFormResponseCommand command) {
        Form form = loadPublishedForm(command.formId());

        if (loadFormResponsePort.existsByFormIdAndMemberId(command.formId(), command.respondentMemberId())) {
            throw new SurveyDomainException(SurveyErrorCode.FORM_RESPONSE_ALREADY_EXISTS);
        }

        validateAnswers(command.formId(), command.answers());

        FormResponse response = FormResponse.createDraft(form, command.respondentMemberId());
        response.submit(Instant.now(), null);
        FormResponse saved = saveFormResponsePort.save(response);

        List<AnswerWithOptions> data = buildAnswerData(saved, command.answers());
        saveAnswers(data);

        return saved.getId();
    }

    @Override
    public void updateResponse(UpdateFormResponseCommand command) {
        loadPublishedForm(command.formId());

        FormResponse existing = loadFormResponsePort
            .findSubmittedByFormIdAndRespondentMemberId(command.formId(), command.respondentMemberId())
            .orElseThrow(() -> new SurveyDomainException(SurveyErrorCode.FORM_RESPONSE_NOT_FOUND));

        validateAnswers(command.formId(), command.answers());

        saveAnswerPort.deleteAllByFormResponseId(existing.getId());

        List<AnswerWithOptions> data = buildAnswerData(existing, command.answers());
        saveAnswers(data);

        existing.updateLastSavedAt(Instant.now());
        saveFormResponsePort.save(existing);
    }

    @Override
    public void deleteResponse(DeleteFormResponseCommand command) {
        FormResponse existing = loadFormResponsePort
            .findSubmittedByFormIdAndRespondentMemberId(command.formId(), command.respondentMemberId())
            .orElseThrow(() -> new SurveyDomainException(SurveyErrorCode.FORM_RESPONSE_NOT_FOUND));

        saveAnswerPort.deleteAllByFormResponseId(existing.getId());
        saveFormResponsePort.deleteById(existing.getId());
    }

    @Override
    public Long createDraft(CreateDraftFormResponseCommand command) {
        // TODO: 구현 PR 에서 실제 로직 작성 — 빈 draft 생성 + 중복 검증
        throw new UnsupportedOperationException("Not implemented yet — Phase C 구현 PR 에서 제공");
    }

    @Override
    public void updateDraft(UpdateDraftFormResponseCommand command) {
        // TODO: 구현 PR 에서 실제 로직 작성 — draft answers 전체 교체
        throw new UnsupportedOperationException("Not implemented yet — Phase C 구현 PR 에서 제공");
    }

    @Override
    public void submitDraft(SubmitDraftFormResponseCommand command) {
        // TODO: 구현 PR 에서 실제 로직 작성 — draft → SUBMITTED 전환
        throw new UnsupportedOperationException("Not implemented yet — Phase C 구현 PR 에서 제공");
    }

    @Override
    public void deleteDraft(DeleteDraftFormResponseCommand command) {
        // TODO: 구현 PR 에서 실제 로직 작성 — draft + 연관 Answer 삭제
        throw new UnsupportedOperationException("Not implemented yet — Phase C 구현 PR 에서 제공");
    }

    private Form loadPublishedForm(Long formId) {
        Form form = loadFormPort.findById(formId)
            .orElseThrow(() -> new SurveyDomainException(SurveyErrorCode.SURVEY_NOT_FOUND));
        if (!form.isPublished()) {
            throw new SurveyDomainException(SurveyErrorCode.SURVEY_NOT_PUBLISHED);
        }
        return form;
    }

    private void validateAnswers(Long formId, List<AnswerCommand> answers) {
        if (answers == null) {
            throw new SurveyDomainException(SurveyErrorCode.INVALID_ANSWER_FORMAT);
        }

        List<Question> formQuestions = loadQuestionPort.listByFormId(formId);

        Set<Long> answeredQuestionIds = new HashSet<>();
        for (AnswerCommand answerCommand : answers) {
            if (answerCommand.questionId() == null) {
                throw new SurveyDomainException(SurveyErrorCode.INVALID_ANSWER_FORMAT);
            }
            if (!answeredQuestionIds.add(answerCommand.questionId())) {
                throw new SurveyDomainException(SurveyErrorCode.INVALID_ANSWER_FORMAT);
            }
            Question question = formQuestions.stream()
                .filter(q -> q.getId().equals(answerCommand.questionId()))
                .findFirst()
                .orElseThrow(() -> new SurveyDomainException(SurveyErrorCode.QUESTION_IS_NOT_OWNED_BY_FORM));

            validateAnswerAgainstQuestion(answerCommand, question);
        }

        for (Question q : formQuestions) {
            if (Boolean.TRUE.equals(q.getIsRequired()) && !answeredQuestionIds.contains(q.getId())) {
                throw new SurveyDomainException(SurveyErrorCode.REQUIRED_QUESTION_NOT_ANSWERED);
            }
        }
    }

    private void validateAnswerAgainstQuestion(AnswerCommand answerCommand, Question question) {
        QuestionType type = question.getType();
        switch (type) {
            case SHORT_TEXT, LONG_TEXT -> {
                if (answerCommand.textValue() == null || answerCommand.textValue().isBlank()) {
                    throw new SurveyDomainException(SurveyErrorCode.INVALID_ANSWER_FORMAT);
                }
            }
            case RADIO, DROPDOWN -> {
                List<Long> selected = answerCommand.selectedOptionIds();
                if (selected == null || selected.size() != 1) {
                    throw new SurveyDomainException(SurveyErrorCode.INVALID_VOTE_SELECTION);
                }
                validateOptionBelongsToQuestion(selected.get(0), question.getId());
            }
            case CHECKBOX -> {
                List<Long> selected = answerCommand.selectedOptionIds();
                if (selected == null || selected.isEmpty()) {
                    throw new SurveyDomainException(SurveyErrorCode.INVALID_VOTE_SELECTION);
                }
                for (Long optionId : selected) {
                    validateOptionBelongsToQuestion(optionId, question.getId());
                }
            }
            case SCHEDULE, FILE, PORTFOLIO ->
                // TODO: 다음 PR에서 Answer 엔티티에 fileIds/times setter 확장 후 구현
                throw new UnsupportedOperationException(
                    "Question type " + type + " is not supported yet");
        }
    }

    private void validateOptionBelongsToQuestion(Long optionId, Long questionId) {
        if (!loadQuestionOptionPort.existsByIdAndQuestionId(optionId, questionId)) {
            throw new SurveyDomainException(SurveyErrorCode.OPTION_NOT_IN_QUESTION);
        }
    }

    private List<AnswerWithOptions> buildAnswerData(FormResponse formResponse, List<AnswerCommand> answers) {
        List<Question> formQuestions = loadQuestionPort.listByFormId(formResponse.getForm().getId());

        List<AnswerWithOptions> result = new ArrayList<>();
        for (AnswerCommand answerCmd : answers) {
            Question question = formQuestions.stream()
                .filter(q -> q.getId().equals(answerCmd.questionId()))
                .findFirst()
                .orElseThrow(() -> new SurveyDomainException(SurveyErrorCode.QUESTION_NOT_FOUND));

            Answer answer = Answer.create(
                formResponse,
                question,
                question.getType(),
                answerCmd.textValue()
            );

            List<QuestionOption> selectedOptions = new ArrayList<>();
            List<Long> optionIds = answerCmd.selectedOptionIds();
            if (optionIds != null && !optionIds.isEmpty()) {
                List<QuestionOption> options = loadQuestionOptionPort.listByQuestionId(question.getId());
                for (Long optionId : optionIds) {
                    QuestionOption option = options.stream()
                        .filter(o -> o.getId().equals(optionId))
                        .findFirst()
                        .orElseThrow(() -> new SurveyDomainException(SurveyErrorCode.OPTION_NOT_IN_QUESTION));
                    selectedOptions.add(option);
                }
            }

            result.add(new AnswerWithOptions(answer, selectedOptions));
        }
        return result;
    }

    private void saveAnswers(List<AnswerWithOptions> data) {
        List<Answer> answers = data.stream().map(AnswerWithOptions::answer).toList();
        List<Answer> savedAnswers = saveAnswerPort.saveAll(answers);

        List<AnswerChoice> choices = new ArrayList<>();
        for (int i = 0; i < savedAnswers.size(); i++) {
            Answer savedAnswer = savedAnswers.get(i);
            for (QuestionOption option : data.get(i).options()) {
                choices.add(new AnswerChoice(savedAnswer, option));
            }
        }
        if (!choices.isEmpty()) {
            saveAnswerPort.saveAllChoices(choices);
        }
    }

    private record AnswerWithOptions(
        Answer answer,
        List<QuestionOption> options
    ) {
    }
}
