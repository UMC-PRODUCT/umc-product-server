package com.umc.product.authentication.adapter.in.oauth;

import com.umc.product.authentication.domain.enums.OAuth2ResultCode;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    // TODO: 전체 다시

    @Value("${app.oauth2.frontend-redirect-url:about:blank}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        MemberPrincipal memberPrincipal = (MemberPrincipal) authentication.getPrincipal();
        Long memberId = memberPrincipal.getMemberId();

        log.info("OAuth2 authentication success: memberId={}", memberId);

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(memberId, Collections.emptyList());
        String refreshToken = jwtTokenProvider.createRefreshToken(memberId);

        OAuth2ResultCode oAuth2ResultCode = OAuth2ResultCode.SUCCESS;

        // 프론트엔드로 리다이렉트 (토큰 전달)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("success", oAuth2ResultCode.isSuccess())
                .queryParam("code", oAuth2ResultCode.getCode())
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        log.info("Redirecting to: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
