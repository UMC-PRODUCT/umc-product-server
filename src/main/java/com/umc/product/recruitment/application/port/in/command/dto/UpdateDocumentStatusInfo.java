package com.umc.product.recruitment.application.port.in.command.dto;

public record UpdateDocumentStatusInfo(
    Long applicationId,
    DocumentResult documentResult
) {
    public record DocumentResult(
        String decision // PASS | WAIT
    ) {
    }
}
