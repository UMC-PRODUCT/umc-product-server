package com.umc.product.inquiry.adapter.in.web.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.umc.product.inquiry.application.port.in.command.dto.CreateInquiryFaqCommand;
import com.umc.product.inquiry.application.port.in.command.dto.InquiryFaqContentCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateInquiryFaqRequest(
    @Schema(description = "공개용 질문. 1~200자")
    @NotNull String question,
    @Schema(description = "공개용 답변. 초안에서는 비워도 되고, 발행하려면 있어야 한다", nullable = true)
    String answer,
    @Schema(description = "검색용 별칭. 사용자 화면에는 보이지 않는다. 20개까지, 하나에 50자")
    List<String> aliases,
    @Schema(description = "문의 분류와 같은 목록의 ID. 발행하려면 있어야 한다", nullable = true)
    @Positive Long categoryId,
    @Schema(description = "대상 ID. 발행하려면 있어야 한다", nullable = true)
    @Positive Long audienceId,
    @Schema(description = "이 날(한국 시간)까지 공개하고 다음 날부터 자동으로 내린다. null이면 계속 공개", nullable = true)
    LocalDate endDate,
    @Schema(description = "관련 안내로 보여 줄 다른 대표 질문 ID", nullable = true)
    @Positive Long relatedFaqId
) {

    public CreateInquiryFaqCommand toCommand(Long actorMemberId) {
        return new CreateInquiryFaqCommand(actorMemberId, new InquiryFaqContentCommand(
            question, answer, aliases, categoryId, audienceId, endDate, relatedFaqId
        ));
    }
}
