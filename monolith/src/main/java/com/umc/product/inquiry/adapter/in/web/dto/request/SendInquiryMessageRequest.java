package com.umc.product.inquiry.adapter.in.web.dto.request;

import java.util.List;
import java.util.UUID;

import com.umc.product.inquiry.application.port.in.command.dto.SendInquiryMessageCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendInquiryMessageRequest(
    String content,
    List<@NotBlank String> fileMetadataIds,
    @Schema(description = "전송 재시도 시 같은 값을 보내면 메시지를 한 번만 남긴다")
    @NotNull UUID clientMessageId
) {

    public SendInquiryMessageCommand toCommand(Long inquiryId, Long senderMemberId) {
        return new SendInquiryMessageCommand(inquiryId, senderMemberId, content, fileMetadataIds, clientMessageId);
    }
}
