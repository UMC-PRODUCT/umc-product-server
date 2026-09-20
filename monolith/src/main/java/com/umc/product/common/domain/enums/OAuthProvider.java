package com.umc.product.common.domain.enums;

import com.umc.product.common.domain.exception.CommonException;
import com.umc.product.global.exception.constant.CommonErrorCode;

public enum OAuthProvider {
    GOOGLE,
    APPLE,
    KAKAO;

    /**
     * registrationId(String)를 OAuthProvider로 변환
     *
     * @param registrationId Spring Security OAuth2의 registrationId (예: "google", "kakao")
     * @return OAuthProvider
     * @throws IllegalArgumentException 지원하지 않는 provider인 경우
     */
    public static OAuthProvider from(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> GOOGLE;
            case "apple" -> APPLE;
            case "kakao" -> KAKAO;
            default -> throw new CommonException(CommonErrorCode.BAD_REQUEST);
        };
    }
}
