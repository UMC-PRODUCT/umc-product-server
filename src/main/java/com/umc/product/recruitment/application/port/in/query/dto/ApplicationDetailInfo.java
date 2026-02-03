package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.enums.ApplicationStatus;
import com.umc.product.survey.domain.enums.QuestionType;
import java.util.List;

public record ApplicationDetailInfo(
        Long applicationId,
        ApplicationStatus status,
        ApplicantInfo applicant,
        List<PageInfo> pages
) {
    public record ApplicantInfo(
            Long memberId,
            String name,
            String nickname
    ) {
    }

    public record PageInfo(
            Integer pageNo,
            List<QuestionInfo> questions
    ) {
    }

    public record QuestionInfo(
            Long questionId,
            Integer orderNo,
            QuestionType type,

            String questionText,
            boolean required,

            List<OptionInfo> options,
            AnswerInfo answer
    ) {
    }

    public record OptionInfo(
            Long optionId,
            String content,
            boolean isOther
    ) {
    }

    public record AnswerInfo(
            QuestionType answeredAsType,
            String displayText,
            Object rawValue
    ) {
    }
}

