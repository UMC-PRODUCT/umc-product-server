package com.umc.product.project.adapter.in.web.dto.request;

import com.umc.product.project.application.port.in.command.dto.TransferProjectOwnershipCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 프로젝트 소유권 양도 요청.
 */
public record TransferProjectOwnershipRequest(
    @NotNull(message = "새 PM의 memberId는 필수입니다")
    Long newOwnerMemberId,

    @Size(max = 200, message = "사유는 200자 이하여야 합니다")
    String reason
) {
    public TransferProjectOwnershipCommand toCommand(Long projectId, Long requesterMemberId) {
        return TransferProjectOwnershipCommand.builder()
            .projectId(projectId)
            .requesterMemberId(requesterMemberId)
            .newOwnerMemberId(newOwnerMemberId)
            .reason(reason)
            .build();
    }
}
