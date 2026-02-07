package com.umc.product.recruitment.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;
import jakarta.validation.constraints.NotBlank;

public record UpdateFinalStatusRequest(
    @NotBlank EvaluationDecision decision,
    ChallengerPart selectedPart
) {
}
