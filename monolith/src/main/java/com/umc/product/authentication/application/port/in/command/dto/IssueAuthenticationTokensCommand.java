package com.umc.product.authentication.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ClientType;

public record IssueAuthenticationTokensCommand(
    Long memberId,
    ClientType clientType
) {

    public static IssueAuthenticationTokensCommand of(Long memberId, ClientType clientType) {
        return new IssueAuthenticationTokensCommand(memberId, clientType);
    }

    public static IssueAuthenticationTokensCommand of(Long memberId) {
        return of(memberId, null);
    }
}
