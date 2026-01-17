package com.umc.product.member.application.port.out;

import com.umc.product.authentication.domain.MemberOAuth;
import com.umc.product.common.domain.enums.OAuthProvider;
import java.util.Optional;

public interface LoadMemberOAuthPort {
    Optional<MemberOAuth> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);

    Optional<MemberOAuth> findByMemberId(Long userId);
}
