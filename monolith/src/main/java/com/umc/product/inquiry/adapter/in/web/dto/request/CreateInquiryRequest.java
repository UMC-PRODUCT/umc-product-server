package com.umc.product.inquiry.adapter.in.web.dto.request;

import java.util.List;
import java.util.UUID;

import com.umc.product.inquiry.application.port.in.command.dto.CreateInquiryCommand;
import com.umc.product.inquiry.domain.enums.InquiryTeam;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateInquiryRequest(
    @NotNull @Positive Long categoryId,
    @Schema(description = "담당 팀. 모르면 null", nullable = true)
    InquiryTeam targetTeam,
    @Schema(description = "문의 내용. 첫 번째 비지 않은 줄(최대 50자)이 제목이 된다")
    @NotNull String content,
    List<@NotBlank String> fileMetadataIds,
    @Schema(description = "등록 재시도 시 같은 값을 보내면 내용 비교 없이 기존 문의를 돌려준다")
    @NotNull UUID clientRequestId
) {

    public CreateInquiryCommand toCommand(Long authorMemberId) {
        return new CreateInquiryCommand(
            authorMemberId,
            categoryId,
            targetTeam,
            content,
            fileMetadataIds,
            clientRequestId
        );
    }
}
