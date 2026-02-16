package com.umc.product.authentication.application.port.in.command.dto;

import lombok.Builder;

@Builder
public record UnlinkOAuthCommand(
    Long memberId,
    Long memberOAuthId,
    boolean bypassValidation // OAuth 계정이 최소 하나는 있어야 한다는 원칙을 bypass (탈퇴용)
) {
}
