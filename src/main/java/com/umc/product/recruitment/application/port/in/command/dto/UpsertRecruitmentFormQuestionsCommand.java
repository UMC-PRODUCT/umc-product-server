package com.umc.product.recruitment.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.survey.domain.enums.QuestionType;
import java.util.List;

public record UpsertRecruitmentFormQuestionsCommand(
        Long recruitmentId,
        List<Item> items
) {
    public record Item(
            Target target,
            QuestionInfo question
    ) {
    }

    public record Target(
            Kind kind,
            Integer pageNo,
            ChallengerPart part
    ) {
        public enum Kind {COMMON_PAGE, PART}
    }

    public record QuestionInfo(
            Long questionId,
            QuestionType type,
            String questionText,
            Boolean required,
            Integer orderNo,
            List<OptionInfo> options
    ) {
    }

    public record OptionInfo(
            Long optionId,
            String content,
            Integer orderNo
    ) {
    }
}
