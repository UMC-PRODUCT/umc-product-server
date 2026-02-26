package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.command.dto.UpdateDocumentStatusInfo;

public record UpdateDocumentStatusResponse(
    Long applicationId,
    DocumentResult documentResult
) {
    public record DocumentResult(
        String decision
    ) {
    }

    public static UpdateDocumentStatusResponse from(UpdateDocumentStatusInfo info) {
        return new UpdateDocumentStatusResponse(
            info.applicationId(),
            new DocumentResult(info.documentResult().decision())
        );
    }
}
