package com.umc.product.authentication.application.port.in.query;

import java.util.List;
import java.util.Set;

import com.umc.product.authentication.application.port.in.query.dto.MemberOAuthInfo;
import com.umc.product.common.domain.enums.OAuthProvider;

public interface GetMemberOAuthUseCase {
    /**
     * 회원과 연동된 OAuth 정보를 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 연동된 OAuth 정보 리스트
     */
    List<MemberOAuthInfo> getOAuthList(Long memberId);

    List<MemberOAuthInfo> listByMemberIds(Set<Long> memberIds);

    MemberOAuthInfo getByProviderAndProviderId(OAuthProvider provider, String providerId);
}
