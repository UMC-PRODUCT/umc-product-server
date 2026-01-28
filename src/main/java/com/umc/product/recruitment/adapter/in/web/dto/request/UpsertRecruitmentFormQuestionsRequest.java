package com.umc.product.recruitment.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormQuestionsCommand;
import com.umc.product.survey.domain.enums.QuestionType;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UpsertRecruitmentFormQuestionsRequest(
        @NotEmpty
        List<Item> items
) {

    public UpsertRecruitmentFormQuestionsCommand toCommand(Long recruitmentId) {
        return new UpsertRecruitmentFormQuestionsCommand(
                recruitmentId,
                items == null
                        ? List.of()
                        : items.stream().map(Item::toCommandItem).toList()
        );
    }

    public record Item(
            Target target,
            Question question
    ) {
        private UpsertRecruitmentFormQuestionsCommand.Item toCommandItem() {
            return new UpsertRecruitmentFormQuestionsCommand.Item(
                    target == null ? null : target.toCommandTarget(),
                    question == null ? null : question.toCommandQuestion()
            );
        }
    }

    public record Target(
            Kind kind,
            Integer pageNo,
            ChallengerPart part
    ) {
        public enum Kind {COMMON_PAGE, PART}

        private UpsertRecruitmentFormQuestionsCommand.Target toCommandTarget() {
            if (kind == null) {
                return null;
            }

            return new UpsertRecruitmentFormQuestionsCommand.Target(
                    UpsertRecruitmentFormQuestionsCommand.Target.Kind.valueOf(kind.name()),
                    pageNo,
                    part
            );
        }
    }

    public record Question(
            Long questionId,
            QuestionType type,
            String questionText,
            Boolean required,
            Integer orderNo,
            List<Option> options
    ) {
        private UpsertRecruitmentFormQuestionsCommand.QuestionInfo toCommandQuestion() {
            return new UpsertRecruitmentFormQuestionsCommand.QuestionInfo(
                    questionId,
                    type,
                    questionText,
                    required,
                    orderNo,
                    options == null ? List.of() : options.stream().map(Option::toCommandOption).toList()
            );
        }
    }

    public record Option(
            Long optionId,
            String content,
            Integer orderNo,
            Boolean isOther
    ) {
        private UpsertRecruitmentFormQuestionsCommand.OptionInfo toCommandOption() {
            return new UpsertRecruitmentFormQuestionsCommand.OptionInfo(optionId, content, orderNo, isOther);
        }
    }
}
