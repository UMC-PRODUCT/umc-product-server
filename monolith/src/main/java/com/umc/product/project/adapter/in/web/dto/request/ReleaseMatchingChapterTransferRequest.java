package com.umc.product.project.adapter.in.web.dto.request;

import com.umc.product.project.application.port.in.command.dto.ReleaseMatchingChapterTransferCommand;

import jakarta.validation.constraints.Size;

public record ReleaseMatchingChapterTransferRequest(
    @Size(max = 255, message = "해제 사유는 255자 이하여야 합니다") String reason
) {

    public ReleaseMatchingChapterTransferCommand toCommand(Long requesterMemberId, Long transferId) {
        return ReleaseMatchingChapterTransferCommand.builder()
            .requesterMemberId(requesterMemberId)
            .transferId(transferId)
            .reason(reason)
            .build();
    }
}
