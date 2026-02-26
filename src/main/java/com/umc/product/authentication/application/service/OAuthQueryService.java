package com.umc.product.authentication.application.service;

import com.umc.product.authentication.application.port.in.query.GetMemberOAuthUseCase;
import com.umc.product.authentication.application.port.in.query.GetOAuthListUseCase;
import com.umc.product.authentication.application.port.in.query.dto.MemberOAuthInfo;
import com.umc.product.authentication.application.port.out.LoadMemberOAuthPort;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.domain.enums.OAuthProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OAuthQueryService implements GetMemberOAuthUseCase, GetOAuthListUseCase {

    private final LoadMemberOAuthPort loadMemberOAuthPort;

    @Override
    public MemberOAuthInfo getMemberByOAuthInfo(OAuthProvider provider, String providerId) {
        return loadMemberOAuthPort.findByProviderAndProviderId(provider, providerId)
            .map(MemberOAuthInfo::fromEntity)
            .orElseThrow(() ->
                new AuthenticationDomainException(AuthenticationErrorCode.NO_MATCHING_MEMBER)
            );
    }


    /**
     * 회원과 연동된 OAuth 정보를 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 연동된 OAuth 정보 리스트
     */
    @Override
    public List<MemberOAuthInfo> getOAuthList(Long memberId) {
        return loadMemberOAuthPort.findAllByMemberId(memberId)
            .stream().map(MemberOAuthInfo::fromEntity).toList();
    }
}
