package com.umc.product.authentication.adapter.in.web;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 소셜 로그인 시작과 callback이 같은 브라우저에서 수행됐는지 확인하는 전용 쿠키를 설정한다.
 * 회원 ID나 토큰은 담지 않으며, 이 쿠키만으로 API 인증이나 SSO 코드 발급을 할 수 없다.
 */
@Component
public class SsoSocialCookieWriter {
    public static final String COOKIE_NAME = "__Host-UMC_SOCIAL_BINDING";

    public void write(HttpServletResponse response, String binding) {
        // HttpOnly는 JavaScript 읽기를 막고 Secure는 HTTPS에서만 전송하게 한다.
        // SameSite=None은 Apple의 다른 사이트에서 돌아오는 form POST에도 쿠키를 보내기 위해 필요하다.
        // __Host- 접두사는 Secure, Path=/, Domain 미지정을 요구해 다른 하위 도메인의 쿠키 주입을 제한한다.
        // 쿠키가 차단되면 callback 검증을 생략하지 않고 실패시킨다. 기존 로그인 쿠키 설정은 유지한다.
        // 하나의 callback 후 삭제하지 않아 같은 브라우저에서 시작한 다른 로그인 시도도 사용할 수 있다.
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, binding).httpOnly(true).secure(true)
            .sameSite("None").path("/").maxAge(Duration.ofMinutes(30)).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
