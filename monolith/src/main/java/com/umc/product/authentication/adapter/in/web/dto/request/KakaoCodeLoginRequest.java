package com.umc.product.authentication.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ClientType;
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
    @NotBlank String redirectUri,

    // 클라이언트 플랫폼(ANDROID/IOS/WEB). 트래픽 분포 분석을 위해 AT claim 으로 흘려보낸다.
    // 도입 이전 클라이언트 호환을 위해 nullable. 누락 시 AT claim 자체가 생략되며
    // 다운스트림 (MDC / 통계) 에서는 "UNKNOWN" 으로 집계된다.
    ClientType clientType
) {
}
