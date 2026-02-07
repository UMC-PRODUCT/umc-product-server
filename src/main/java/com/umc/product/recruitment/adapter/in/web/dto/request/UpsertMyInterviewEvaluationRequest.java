package com.umc.product.recruitment.adapter.in.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpsertMyInterviewEvaluationRequest(
    @NotNull @Min(0) @Max(100) Integer score,
    @Size(max = 120) String comments
) {
}
