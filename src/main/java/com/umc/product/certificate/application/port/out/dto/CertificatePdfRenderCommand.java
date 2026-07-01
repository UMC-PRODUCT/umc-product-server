package com.umc.product.certificate.application.port.out.dto;

import java.time.Instant;
import java.util.Objects;

import com.umc.product.certificate.domain.CertificateType;

import lombok.Builder;

@Builder
public record CertificatePdfRenderCommand(
    String serialNumber,
    CertificateType type,
    String recipientName,
    String recipientSchoolName,
    Long gisuGeneration,
    String projectName,
    String awardTitle,
    String awardDescription,
    Instant issuedAt,
    Instant expiresAt,
    String verificationUrl
) {

    public CertificatePdfRenderCommand {
        Objects.requireNonNull(serialNumber, "serialNumber must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(recipientName, "recipientName must not be null");
        Objects.requireNonNull(gisuGeneration, "gisuGeneration must not be null");
        Objects.requireNonNull(issuedAt, "issuedAt must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        Objects.requireNonNull(verificationUrl, "verificationUrl must not be null");
    }
}
