package com.umc.product.recruitment.adapter.in.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateInterviewAssignmentRequest(
    @NotNull Long applicationId,
    @Valid @NotNull To to
) {
    public record To(@NotNull Long slotId) {
    }
}
