package com.umc.product.inquiry.adapter.in.web.dto.request;

import java.util.List;
import java.util.UUID;

import com.umc.product.inquiry.application.port.in.command.dto.CreateInquiryCommand;
import com.umc.product.inquiry.domain.enums.InquiryTeam;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateInquiryRequest(
    @NotNull @Positive Long categoryId,
    @Schema(description = "담당 팀. 모르면 null", nullable = true)
    InquiryTeam targetTeam,
    @NotBlank @Size(max = 100) String title,
    @NotBlank @Size(max = 2000) String content,
    @Size(max = 5) List<@NotBlank String> fileMetadataIds,
    @Schema(description = "등록 재시도 시 같은 값을 보내면 기존 문의를 돌려준다")
    @NotNull UUID clientRequestId
) {

    public CreateInquiryCommand toCommand(Long authorMemberId) {
        return new CreateInquiryCommand(
            authorMemberId,
            categoryId,
            targetTeam,
            title,
            content,
            fileMetadataIds,
            clientRequestId
        );
    }
}
