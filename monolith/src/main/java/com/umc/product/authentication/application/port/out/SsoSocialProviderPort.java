package com.umc.product.authentication.application.port.out;

import com.umc.product.authentication.domain.OAuthAttributes;
import com.umc.product.common.domain.enums.OAuthProvider;

/** 제공자별 HTTP/토큰 형식을 application에서 숨기는 외부 인증 계약이다. */
public interface SsoSocialProviderPort {
    /** 서버가 생성한 state/nonce로 인가 URL과 실제 서버 callback 주소를 준비한다. 네트워크 교환은 하지 않는다. */
    Authorization prepare(OAuthProvider provider, String state, String nonce);

    /** 제공자 code를 서버에서 교환하고, 검증된 소셜 신원을 반환한다. UMC code/token을 만드는 단계는 아니다. */
    Identity exchange(OAuthProvider provider, String code, String callbackUri, String nonce);

    record Authorization(String uri, String callbackUri) {
        @Override
        public String toString() {
            return "SocialAuthorization[redacted]";
        }
    }

    record Identity(OAuthAttributes attributes, String appleRefreshToken, String clientId) {
        @Override
        public String toString() {
            return "SocialIdentity[redacted]";
        }
    }
}
