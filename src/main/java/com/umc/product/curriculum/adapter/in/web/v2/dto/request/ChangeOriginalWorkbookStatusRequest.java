package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import com.umc.product.curriculum.application.port.in.command.dto.workbook.ChangeOriginalWorkbookStatusCommand;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ChangeOriginalWorkbookStatusRequest(
    @NotNull(message = "원본 워크북 ID는 필수입니다")
    Long originalWorkbookId,

    @NotNull(message = "변경할 상태는 필수입니다")
    OriginalWorkbookStatus status
) {
    public ChangeOriginalWorkbookStatusCommand toCommand(Long requestedMemberId) {
        return ChangeOriginalWorkbookStatusCommand.builder()
            .originalWorkbookId(originalWorkbookId)
            .status(status)
            .requestedMemberId(requestedMemberId)
            .build();
    }
}