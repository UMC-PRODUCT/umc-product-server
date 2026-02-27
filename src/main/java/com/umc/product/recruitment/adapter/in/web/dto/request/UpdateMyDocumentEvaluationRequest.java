package com.umc.product.recruitment.adapter.in.web.dto.request;

import com.umc.product.recruitment.domain.enums.DocumentEvaluationAction;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateMyDocumentEvaluationRequest(
    @NotNull DocumentEvaluationAction action,
    @Min(0) @Max(100) Integer score,
    @Size(max = 80) String comments
) {
}
