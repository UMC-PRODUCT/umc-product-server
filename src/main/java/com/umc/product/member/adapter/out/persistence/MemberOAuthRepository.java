package com.umc.product.member.adapter.out.persistence;

import com.umc.product.member.domain.MemberOAuth;
import com.umc.product.member.domain.OAuthProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberOAuthRepository extends JpaRepository<MemberOAuth, Long> {
    Optional<MemberOAuth> findByProviderAndProviderId(OAuthProvider provider, String providerId);

    Optional<MemberOAuth> findByMemberId(Long memberId);
}
