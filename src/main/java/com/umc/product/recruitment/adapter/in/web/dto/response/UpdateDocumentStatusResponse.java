package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.command.dto.UpdateDocumentStatusInfo;
import com.umc.product.recruitment.domain.enums.ApplicationStatus;
import java.time.Instant;

public record UpdateDocumentStatusResponse(
    Long applicationId,
    ApplicationStatus applicationStatus,
    Double averageScore,
    Instant updatedAt
) {
    public static UpdateDocumentStatusResponse from(UpdateDocumentStatusInfo info) {
        return new UpdateDocumentStatusResponse(
            info.applicationId(),
            info.applicationStatus(),
            info.averageScore(),
            info.updatedAt()
        );
    }
}
