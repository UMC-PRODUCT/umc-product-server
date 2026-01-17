package com.umc.product.authentication.adapter.out.persistence;

import com.umc.product.authentication.domain.MemberOAuth;
import com.umc.product.common.domain.enums.OAuthProvider;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberOAuthRepository extends JpaRepository<MemberOAuth, Long> {
    Optional<MemberOAuth> findByProviderAndProviderId(OAuthProvider provider, String providerId);

    List<MemberOAuth> findAllByMemberId(Long memberId);
}
