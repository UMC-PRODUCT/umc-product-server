package com.umc.product.authentication.application.service;

import com.umc.product.authentication.application.port.in.query.GetMemberOAuthUseCase;
import com.umc.product.authentication.application.port.in.query.dto.MemberOAuthInfo;
import com.umc.product.authentication.application.port.out.LoadMemberOAuthPort;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.domain.enums.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OAuthQueryService implements GetMemberOAuthUseCase {

    private final LoadMemberOAuthPort loadMemberOAuthPort;

    @Override
    public MemberOAuthInfo getMemberByOAuthInfo(OAuthProvider provider, String providerId) {
        return loadMemberOAuthPort.findByProviderAndProviderId(provider, providerId)
            .map(MemberOAuthInfo::fromEntity)
            .orElseThrow(() ->
                new AuthenticationDomainException(AuthenticationErrorCode.NO_MATCHING_MEMBER)
            );
    }
}
