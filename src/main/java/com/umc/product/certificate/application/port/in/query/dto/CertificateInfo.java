package com.umc.product.certificate.application.port.in.query.dto;

import java.time.Instant;

import com.umc.product.certificate.domain.Certificate;
import com.umc.product.certificate.domain.CertificateStatus;
import com.umc.product.certificate.domain.CertificateType;

public record CertificateInfo(
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

    public static CertificateInfo from(Certificate certificate, Instant now) {
        return new CertificateInfo(
            certificate.getId(),
            certificate.getSerialNumber(),
            certificate.getType(),
            certificate.statusAt(now),
            certificate.getRecipientName(),
            certificate.getGisuId(),
            certificate.getGisuGeneration(),
            certificate.getProjectId(),
            certificate.getProjectName(),
            certificate.getAwardTitle(),
            certificate.getIssuedAt(),
            certificate.getExpiresAt()
        );
    }
}
