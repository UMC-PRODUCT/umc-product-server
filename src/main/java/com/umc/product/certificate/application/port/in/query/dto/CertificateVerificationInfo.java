package com.umc.product.certificate.application.port.in.query.dto;

import java.time.Instant;

import com.umc.product.certificate.domain.Certificate;
import com.umc.product.certificate.domain.CertificateStatus;
import com.umc.product.certificate.domain.CertificateType;

public record CertificateVerificationInfo(
    boolean valid,
    String status,
    CertificateType type,
    String recipientName,
    Instant issuedAt,
    Instant expiresAt
) {

    public static CertificateVerificationInfo notFound() {
        return new CertificateVerificationInfo(false, "NOT_FOUND", null, null, null, null);
    }

    public static CertificateVerificationInfo from(Certificate certificate, Instant now, String maskedName) {
        CertificateStatus status = certificate.statusAt(now);
        return new CertificateVerificationInfo(
            status == CertificateStatus.ISSUED,
            status.name(),
            certificate.getType(),
            maskedName,
            certificate.getIssuedAt(),
            certificate.getExpiresAt()
        );
    }
}
