package com.umc.product.certificate.application.port.in.command.dto;

import java.util.Objects;

import com.umc.product.certificate.domain.CertificateIssuer;
import com.umc.product.certificate.domain.CertificateType;

import lombok.Builder;

@Builder
public record AdminIssueCertificateCommand(
    CertificateType type,
    CertificateIssuer issuer,
    Long requesterMemberId,
    Long recipientMemberId,
    Long gisuId,
    Long projectId,
    String meritTitle,
    String meritDescription,
    boolean reissue
) {

    public AdminIssueCertificateCommand {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(requesterMemberId, "requesterMemberId must not be null");
        Objects.requireNonNull(recipientMemberId, "recipientMemberId must not be null");
        Objects.requireNonNull(gisuId, "gisuId must not be null");
    }
}
