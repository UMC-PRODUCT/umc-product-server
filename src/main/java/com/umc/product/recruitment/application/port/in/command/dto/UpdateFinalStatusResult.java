package com.umc.product.recruitment.application.port.in.command.dto;

import com.umc.product.recruitment.domain.enums.PartKey;

public record UpdateFinalStatusResult(
        Long applicationId,
        FinalResult finalResult
) {
    public record FinalResult(
            String decision,
            PartKey selectedPart
    ) {
    }
}