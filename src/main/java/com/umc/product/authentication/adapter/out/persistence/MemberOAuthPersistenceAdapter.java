package com.umc.product.authentication.adapter.out.persistence;

import com.umc.product.authentication.application.port.out.LoadMemberOAuthPort;
import com.umc.product.authentication.application.port.out.SaveMemberOAuthPort;
import com.umc.product.authentication.domain.MemberOAuth;
import com.umc.product.common.domain.enums.OAuthProvider;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MemberOAuthPersistenceAdapter implements LoadMemberOAuthPort, SaveMemberOAuthPort {

    private final MemberOAuthRepository memberOAuthRepository;

    @Override
    public Optional<MemberOAuth> findByProviderAndProviderId(OAuthProvider provider, String providerId) {
        return memberOAuthRepository.findByProviderAndProviderId(
                provider, providerId
        );
    }

    @Override
    public Optional<MemberOAuth> findByMemberOAuthId(Long memberOAuthId) {
        return memberOAuthRepository.findById(memberOAuthId);
    }

    @Override
    public List<MemberOAuth> findAllByMemberId(Long memberId) {
        return memberOAuthRepository.findAllByMemberId(memberId);
    }

    @Override
    public MemberOAuth save(MemberOAuth memberOAuth) {
        return memberOAuthRepository.save(memberOAuth);
    }

    @Override
    public void delete(MemberOAuth memberOAuth) {
        memberOAuthRepository.delete(memberOAuth);
    }
}
