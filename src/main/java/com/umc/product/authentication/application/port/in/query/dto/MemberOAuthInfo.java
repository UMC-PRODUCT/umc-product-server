package com.umc.product.authentication.application.port.in.query.dto;

import com.umc.product.authentication.domain.MemberOAuth;
import com.umc.product.common.domain.enums.OAuthProvider;
import lombok.Builder;

@Builder
public record MemberOAuthInfo(
        Long memberOAuthId,
        Long memberId,
        OAuthProvider provider
) {
    public static MemberOAuthInfo fromEntity(MemberOAuth memberOAuth) {
        return MemberOAuthInfo.builder()
                .memberOAuthId(memberOAuth.getId())
                .memberId(memberOAuth.getMemberId())
                .provider(memberOAuth.getProvider())
                .build();
    }
}
