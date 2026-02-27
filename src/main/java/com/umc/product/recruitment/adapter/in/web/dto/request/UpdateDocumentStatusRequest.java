package com.umc.product.recruitment.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateDocumentStatusRequest(
    @NotNull EvaluationDecision decision
) {
}
