package com.umc.product.authentication.adapter.out.external;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;

import io.jsonwebtoken.Claims;

final class OidcLoginClaimsValidator {
    private OidcLoginClaimsValidator() {
    }

    /**
     * 각 verifier의 서명·issuer·audience 검증에 더해 서버 로그인에 필요한 claim을 확인한다.
     * nonce는 이번 시도와 토큰을 연결하며, exp와 sub는 유효 기간과 회원 식별을 위해 반드시 필요하다.
     * azp(authorized party)가 있으면 우리 client와 일치해야 하고, audience가 여러 개면 azp도 요구한다.
     * 다른 클라이언트나 다른 로그인 시도에 발급된 토큰을 서명만 맞는다는 이유로 받아들이지 않기 위함이다.
     */
    static void validate(Claims claims, String clientId, String nonce) {
        String actual = claims.get("nonce", String.class);
        String authorizedParty = claims.get("azp", String.class);
        if (actual == null || claims.getExpiration() == null || claims.getSubject() == null
            || claims.getSubject().isBlank() || !MessageDigest.isEqual(
                nonce.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8))
            || (authorizedParty != null && !clientId.equals(authorizedParty))
            || (claims.getAudience().size() > 1 && authorizedParty == null)) {
            // 기대값과 실제 nonce를 예외 메시지에 포함하지 않는다.
            throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_TOKEN_VERIFICATION_FAILED);
        }
    }
}
