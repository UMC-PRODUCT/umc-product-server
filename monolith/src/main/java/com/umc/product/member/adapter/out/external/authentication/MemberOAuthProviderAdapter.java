package com.umc.product.member.adapter.out.external.authentication;

import java.util.List;

import org.springframework.stereotype.Component;

import com.umc.product.authentication.application.port.in.query.GetMemberOAuthUseCase;
import com.umc.product.authentication.application.port.in.query.dto.MemberOAuthInfo;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.member.application.port.out.LoadMemberOAuthProviderPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberOAuthProviderAdapter implements LoadMemberOAuthProviderPort {

    private final GetMemberOAuthUseCase getMemberOAuthUseCase;

    @Override
    public List<OAuthProvider> listByMemberId(Long memberId) {
        return getMemberOAuthUseCase.getOAuthList(memberId).stream()
            .map(MemberOAuthInfo::provider)
            .distinct()
            .sorted()
            .toList();
    }
}
