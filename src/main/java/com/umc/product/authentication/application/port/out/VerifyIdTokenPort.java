package com.umc.product.authentication.application.port.out;

import com.umc.product.authentication.adapter.in.oauth.OAuth2Attributes;
import com.umc.product.common.domain.enums.OAuthProvider;

/**
 * OAuth ID 토큰 검증 Port
 * <p>
 * Service가 외부 OAuth Provider에 토큰 검증을 요청하기 위한 Outbound Port입니다.
 */
public interface VerifyIdTokenPort {
    /**
     * ID 토큰(또는 Access Token)을 검증하고 사용자 정보를 추출합니다.
     *
     * @param provider OAuth Provider (GOOGLE, KAKAO 등)
     * @param token    검증할 토큰 (Google: ID Token, Kakao: Access Token)
     * @return OAuth2Attributes (기존 클래스 재사용)
     * @throws com.umc.product.authentication.domain.exception.AuthenticationDomainException 토큰 검증 실패 시
     */
    OAuth2Attributes verify(OAuthProvider provider, String token);
}
