package com.umc.product.recruitment.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;
import jakarta.validation.constraints.NotNull;

public record UpdateFinalStatusRequest(
        @NotNull EvaluationDecision decision,
        ChallengerPart selectedPart
) {
}