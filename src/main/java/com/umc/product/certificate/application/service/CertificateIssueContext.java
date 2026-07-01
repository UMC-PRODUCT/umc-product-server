package com.umc.product.certificate.application.service;

import com.umc.product.certificate.domain.CertificateType;

record CertificateIssueContext(
    CertificateType type,
    Long recipientMemberId,
    String recipientName,
    String recipientSchoolName,
    Long gisuId,
    Long gisuGeneration,
    Long projectId,
    String projectName,
    String awardTitle,
    String awardDescription,
    Long issuedByMemberId
) {
}
