package com.umc.product.authentication.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Kakao Authorization Code 기반 로그인 요청.
 * <p>
 * 표준 OAuth2 authorization code grant 흐름을 사용하는 클라이언트(주로 웹)가
 * Kakao에서 받은 authorization code와 인가 요청 시 사용한 redirect URI를 전달합니다.
 * 모바일 네이티브 SDK 사용 시에는 기존 {@link KakaoLoginRequest}(access token) 흐름을 사용해주세요.
 */
public record KakaoCodeLoginRequest(
    @NotBlank String authorizationCode,
    @NotBlank String redirectUri
) {
}
