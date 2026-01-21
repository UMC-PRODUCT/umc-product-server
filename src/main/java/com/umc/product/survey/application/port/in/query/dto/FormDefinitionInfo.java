package com.umc.product.survey.application.port.in.query.dto;

import com.umc.product.survey.domain.Form;
import com.umc.product.survey.domain.FormSection;
import com.umc.product.survey.domain.Question;
import com.umc.product.survey.domain.QuestionOption;
import com.umc.product.survey.domain.enums.FormSectionType;
import com.umc.product.survey.domain.enums.QuestionType;
import java.util.Comparator;
import java.util.List;

public record FormDefinitionInfo(
        Long formId,
        String title,
        String description,
        List<FormSectionInfo> sections
) {
    public record FormSectionInfo(
            Long sectionId,
            FormSectionType type,
            String targetKey,
            String title,
            String description,
            int orderNo,
            List<QuestionInfo> questions
    ) {
    }

    public record QuestionInfo(
            Long questionId,
            String questionText,
            QuestionType type,
            boolean isRequired,
            int orderNo,
            List<QuestionOptionInfo> options // 객관식 아닌 경우 empty
    ) {
    }

    public record QuestionOptionInfo(
            Long optionId,
            String content,
            int orderNo
    ) {
    }

    public static FormDefinitionInfo from(Form form) {
        return new FormDefinitionInfo(
                form.getId(),
                form.getTitle(),
                form.getDescription(),
                form.getSections().stream()
                        .sorted(Comparator.comparingInt(FormSection::getOrderNo))
                        .map(FormDefinitionInfo::toSection)
                        .toList()
        );
    }

    private static FormDefinitionInfo.FormSectionInfo toSection(FormSection s) {
        return new FormDefinitionInfo.FormSectionInfo(
                s.getId(),
                s.getType(),
                s.getTargetKey(),
                s.getTitle(),
                s.getDescription(),
                s.getOrderNo(),
                s.getQuestions().stream()
                        .sorted(Comparator.comparingInt(Question::getOrderNo))
                        .map(FormDefinitionInfo::toQuestion)
                        .toList()
        );
    }

    private static FormDefinitionInfo.QuestionInfo toQuestion(Question q) {
        return new FormDefinitionInfo.QuestionInfo(
                q.getId(),
                q.getQuestionText(),
                q.getType(),
                q.getIsRequired(),
                q.getOrderNo(),
                q.getOptions() == null ? List.of()
                        : q.getOptions().stream()
                                .sorted(Comparator.comparingInt(QuestionOption::getOrderNo))
                                .map(o -> new FormDefinitionInfo.QuestionOptionInfo(
                                        o.getId(),
                                        o.getContent(),
                                        o.getOrderNo()
                                ))
                                .toList()
        );
    }
}
