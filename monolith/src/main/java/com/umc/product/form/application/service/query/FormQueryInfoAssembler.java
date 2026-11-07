package com.umc.product.form.application.service.query;

import java.util.List;

import com.umc.product.form.application.port.in.query.dto.AnswerInfo;
import com.umc.product.form.application.port.in.query.dto.FormInfo;
import com.umc.product.form.application.port.in.query.dto.FormResponseInfo;
import com.umc.product.form.application.port.in.query.dto.FormResponseWithAnswersInfo;
import com.umc.product.form.application.port.in.query.dto.FormSectionInfo;
import com.umc.product.form.application.port.in.query.dto.QuestionInfo;
import com.umc.product.form.application.port.in.query.dto.QuestionOptionInfo;
import com.umc.product.form.domain.Answer;
import com.umc.product.form.domain.AnswerChoice;
import com.umc.product.form.domain.Form;
import com.umc.product.form.domain.FormResponse;
import com.umc.product.form.domain.FormSection;
import com.umc.product.form.domain.Question;
import com.umc.product.form.domain.QuestionOption;

/**
 * Form 엔티티를 공개 조회 DTO로 변환한다.
 */
final class FormQueryInfoAssembler {

    private FormQueryInfoAssembler() {
    }

    static FormInfo toFormInfo(Form form) {
        return FormInfo.builder()
            .id(form.getId())
            .createdMemberId(form.getCreatedMemberId())
            .title(form.getTitle())
            .description(form.getDescription())
            .status(form.getStatus())
            .isAnonymous(form.isAnonymous())
            .allowDuplicateResponses(form.isAllowDuplicateResponses())
            .createdAt(form.getCreatedAt())
            .updatedAt(form.getUpdatedAt())
            .build();
    }

    static FormSectionInfo toFormSectionInfo(FormSection section) {
        return FormSectionInfo.builder()
            .sectionId(section.getId())
            .formId(section.getForm().getId())
            .title(section.getTitle())
            .description(section.getDescription())
            .orderNo(section.getOrderNo())
            .createdAt(section.getCreatedAt())
            .updatedAt(section.getUpdatedAt())
            .build();
    }

    static QuestionInfo toQuestionInfo(Question question) {
        return QuestionInfo.builder()
            .id(question.getId())
            .sectionId(question.getFormSection().getId())
            .type(question.getType())
            .title(question.getTitle())
            .description(question.getDescription())
            .isRequired(Boolean.TRUE.equals(question.getIsRequired()))
            .orderNo(question.getOrderNo())
            .createdAt(question.getCreatedAt())
            .updatedAt(question.getUpdatedAt())
            .build();
    }

    static QuestionOptionInfo toQuestionOptionInfo(QuestionOption option) {
        return QuestionOptionInfo.builder()
            .id(option.getId())
            .questionId(option.getQuestion().getId())
            .content(option.getContent())
            .orderNo(option.getOrderNo())
            .isOther(option.isOther())
            .nextSectionId(option.getNextSectionId())
            .createdAt(option.getCreatedAt())
            .updatedAt(option.getUpdatedAt())
            .build();
    }

    /**
     * 객관식이 아닌 답변의 {@code choices}는 빈 리스트다.
     */
    static AnswerInfo toAnswerInfo(Answer answer, List<AnswerChoice> choices) {
        List<AnswerInfo.SelectedOption> selectedOptions = choices.stream()
            .map(FormQueryInfoAssembler::toSelectedOption)
            .toList();

        return AnswerInfo.builder()
            .id(answer.getId())
            .formResponseId(answer.getFormResponse().getId())
            .questionId(answer.getQuestion().getId())
            .answeredAsType(answer.getAnsweredAsType())
            .textValue(answer.getTextValue())
            .selectedOptions(selectedOptions)
            .fileIds(answer.getFileIds())
            .times(answer.getTimes())
            .createdAt(answer.getCreatedAt())
            .updatedAt(answer.getUpdatedAt())
            .build();
    }

    private static AnswerInfo.SelectedOption toSelectedOption(AnswerChoice choice) {
        QuestionOption option = choice.getQuestionOption();
        return AnswerInfo.SelectedOption.builder()
            .questionOptionId(option != null ? option.getId() : null)
            .answeredAsContent(choice.getAnsweredAsContent())
            .build();
    }

    static FormResponseInfo toFormResponseInfo(FormResponse formResponse) {
        return FormResponseInfo.builder()
            .id(formResponse.getId())
            .formId(formResponse.getForm().getId())
            .respondentMemberId(formResponse.getRespondentMemberId())
            .status(formResponse.getStatus())
            .submittedAt(formResponse.getSubmittedAt())
            .submittedIp(formResponse.getSubmittedIp())
            .lastSavedAt(formResponse.getLastSavedAt())
            .createdAt(formResponse.getCreatedAt())
            .updatedAt(formResponse.getUpdatedAt())
            .build();
    }

    static FormResponseWithAnswersInfo toFormResponseWithAnswersInfo(
        FormResponse formResponse, List<AnswerInfo> answers
    ) {
        return FormResponseWithAnswersInfo.builder()
            .id(formResponse.getId())
            .formId(formResponse.getForm().getId())
            .respondentMemberId(formResponse.getRespondentMemberId())
            .status(formResponse.getStatus())
            .submittedAt(formResponse.getSubmittedAt())
            .submittedIp(formResponse.getSubmittedIp())
            .lastSavedAt(formResponse.getLastSavedAt())
            .createdAt(formResponse.getCreatedAt())
            .updatedAt(formResponse.getUpdatedAt())
            .answers(answers)
            .build();
    }
}
