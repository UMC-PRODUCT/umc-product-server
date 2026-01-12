package com.umc.product.authentication.application.port.in.query.dto;

import com.umc.product.common.domain.enums.OAuthProvider;

public record MemberOAuthInfo(
        Long memberId,
        OAuthProvider provider
) {
}
