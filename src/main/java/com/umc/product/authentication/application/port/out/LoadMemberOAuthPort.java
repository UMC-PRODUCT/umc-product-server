package com.umc.product.authentication.application.port.out;

import com.umc.product.authentication.domain.MemberOAuth;
import com.umc.product.common.domain.enums.OAuthProvider;
import java.util.List;
import java.util.Optional;

public interface LoadMemberOAuthPort {
    /**
     * OAuth Provider와 Provider ID로 MemberOAuth를 가져옵니다.
     */
    Optional<MemberOAuth> findByProviderAndProviderId(
            OAuthProvider provider,
            String providerId
    );

    /**
     * MemberOAuthId로 MemberOAuth를 가져옵니다.
     */
    Optional<MemberOAuth> findByMemberOAuthId(Long memberOAuthId);

    /**
     * memberId에 연동된 모든 MemberOAuth를 가져옵니다.
     */
    List<MemberOAuth> findAllByMemberId(Long memberId);

    Optional<MemberOAuth> findByMemberIdAndProvider(Long memberId, OAuthProvider provider);
}
