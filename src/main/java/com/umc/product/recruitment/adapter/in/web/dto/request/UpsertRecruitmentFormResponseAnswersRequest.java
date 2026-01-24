package com.umc.product.recruitment.adapter.in.web.dto.request;

import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormResponseAnswersCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormResponseAnswersCommand.UpsertItem;
import com.umc.product.survey.domain.enums.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Schema(name = "UpsertRecruitmentFormResponseAnswersRequest")
public record UpsertRecruitmentFormResponseAnswersRequest(
        @NotEmpty(message = "items는 비어 있을 수 없습니다")
        List<Item> items
) {
    @Schema(name = "UpsertRecruitmentFormResponseAnswersItem")
    public record Item(
            @NotNull(message = "questionId는 필수입니다")
            Long questionId,

            /**
             * answeredAsType은 서버가 Question 조회로 확정할 수 있지만,
             * 스웨거/검증 용도로 내려받아 함께 보내는 형태도 허용.
             */
            QuestionType answeredAsType,

            @NotNull(message = "value는 필수입니다")
            Map<String, Object> value
    ) {
    }

    public UpsertRecruitmentFormResponseAnswersCommand toCommand(Long memberId, Long recruitmentId,
                                                                 Long formResponseId) {
        return new UpsertRecruitmentFormResponseAnswersCommand(
                memberId,
                recruitmentId,
                formResponseId,
                items.stream()
                        .map(i -> new UpsertItem(i.questionId(), i.answeredAsType(), i.value()))
                        .toList()
        );
    }
}
