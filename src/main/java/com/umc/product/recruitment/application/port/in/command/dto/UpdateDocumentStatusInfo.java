package com.umc.product.recruitment.application.port.in.command.dto;

import com.umc.product.recruitment.domain.enums.ApplicationStatus;
import java.time.Instant;

public record UpdateDocumentStatusInfo(
        Long applicationId,
        ApplicationStatus applicationStatus,
        Double averageScore,
        Instant updatedAt
) {
}
