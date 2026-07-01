package com.umc.product.certificate.adapter.in.web.dto.response;

import java.time.Instant;

import com.umc.product.certificate.application.port.in.query.dto.CertificateInfo;
import com.umc.product.certificate.domain.CertificateStatus;
import com.umc.product.certificate.domain.CertificateType;

public record CertificateResponse(
    Long certificateId,
    String serialNumber,
    CertificateType type,
    CertificateStatus status,
    String recipientName,
    Long gisuId,
    Long gisuGeneration,
    Long projectId,
    String projectName,
    String awardTitle,
    Instant issuedAt,
    Instant expiresAt
) {

    public static CertificateResponse from(CertificateInfo info) {
        return new CertificateResponse(
            info.certificateId(),
            info.serialNumber(),
            info.type(),
            info.status(),
            info.recipientName(),
            info.gisuId(),
            info.gisuGeneration(),
            info.projectId(),
            info.projectName(),
            info.awardTitle(),
            info.issuedAt(),
            info.expiresAt()
        );
    }
}
