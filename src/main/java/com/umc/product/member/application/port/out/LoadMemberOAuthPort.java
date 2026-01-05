package com.umc.product.member.application.port.out;

import com.umc.product.member.domain.MemberOAuth;
import com.umc.product.member.domain.OAuthProvider;
import java.util.Optional;

public interface LoadMemberOAuthPort {
    Optional<MemberOAuth> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);

    Optional<MemberOAuth> findByUserId(Long userId);
}
