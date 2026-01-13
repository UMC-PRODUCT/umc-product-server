package com.umc.product.survey.application.port.in.query.dto;

import com.umc.product.survey.domain.enums.FormSectionType;
import com.umc.product.survey.domain.enums.QuestionType;
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
}
