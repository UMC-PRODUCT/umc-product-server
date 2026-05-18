package com.umc.product.authentication.application.port.out;

import com.umc.product.authentication.adapter.out.external.OAuthAttributes;
import com.umc.product.common.domain.enums.ClientType;
import com.umc.product.common.domain.enums.OAuthProvider;

/**
 * OAuth ID 토큰 검증 Port
 * <p>
 * Service가 외부 OAuth Provider에 토큰 검증을 요청하기 위한 Outbound Port입니다.
 */
public interface VerifyOAuthTokenPort {
    /**
     * ID 토큰(또는 Access Token)을 검증하고 사용자 정보를 추출합니다.
     *
     * @param provider OAuth Provider (GOOGLE, KAKAO 등)
     * @param token    검증할 토큰 (Google: ID Token, Kakao: Access Token)
     * @return OAuthAttributes (기존 클래스 재사용)
     * @throws com.umc.product.authentication.domain.exception.AuthenticationDomainException 토큰 검증 실패 시
     */
    OAuthAttributes verify(OAuthProvider provider, String token);

    /**
     * Authorization Code를 교환하여 사용자 정보를 추출합니다.
     * <p>
     * Apple은 별도 {@link #verifyAppleAuthorizationCode}를 사용하므로 이 메서드는 호출하지 않습니다.
     * Apple은 클라이언트 플랫폼별 client_id 분기와 refresh token 반환이 필요하기 때문입니다.
     *
     * @param provider          OAuth Provider (현재는 KAKAO만 지원)
     * @param authorizationCode 발급받은 authorization code
     * @param redirectUri       인가 요청에 사용한 redirect URI (token 교환 시 동일 값이어야 함)
     * @return OAuthAttributes
     * @throws com.umc.product.authentication.domain.exception.AuthenticationDomainException
     *     지원하지 않는 provider이거나 코드 교환 실패 시
     */
    OAuthAttributes verifyAuthorizationCode(OAuthProvider provider, String authorizationCode, String redirectUri);

    /**
     * Apple Authorization Code를 교환하여 사용자 정보를 추출합니다.
     * <p>
     * Apple은 플랫폼별로 다른 client_id(Bundle ID vs Services ID)를 사용하므로
     * code 교환 시 클라이언트 플랫폼 정보가 필요합니다.
     *
     * @param authorizationCode Apple에서 발급받은 authorization code
     * @param clientType        클라이언트 플랫폼
     * @return Apple authorization code 교환 결과 (사용자 정보 + refresh token + 사용된 client_id)
     * @throws com.umc.product.authentication.domain.exception.AuthenticationDomainException 코드 교환 또는 토큰 검증 실패 시
     */
    AppleAuthorizationCodeResult verifyAppleAuthorizationCode(String authorizationCode, ClientType clientType);
}