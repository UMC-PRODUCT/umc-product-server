package com.umc.product.certificate.adapter.in.web.dto.request;

import com.umc.product.certificate.application.port.in.command.dto.AdminIssueCertificateCommand;
import com.umc.product.certificate.domain.CertificateIssuer;
import com.umc.product.certificate.domain.CertificateType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminIssueCertificateRequest(
    @NotNull(message = "인증서 종류는 필수입니다.") CertificateType type,

    CertificateIssuer issuer,

    @NotNull(message = "수신자 회원 ID는 필수입니다.") Long recipientMemberId,

    @NotNull(message = "기수 ID는 필수입니다.") Long gisuId,

    Long projectId,

    @Size(max = 100, message = "공로증 제목은 100자 이하로 입력해주세요.") String meritTitle,

    @Size(max = 500, message = "공로증 설명은 500자 이하로 입력해주세요.") String meritDescription,

    boolean reissue
) {

    public AdminIssueCertificateCommand toCommand(Long requesterMemberId) {
        return AdminIssueCertificateCommand.builder()
            .type(type)
            .issuer(issuer)
            .requesterMemberId(requesterMemberId)
            .recipientMemberId(recipientMemberId)
            .gisuId(gisuId)
            .projectId(projectId)
            .meritTitle(meritTitle)
            .meritDescription(meritDescription)
            .reissue(reissue)
            .build();
    }
}
