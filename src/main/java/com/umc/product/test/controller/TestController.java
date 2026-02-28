package com.umc.product.test.controller;

import com.umc.product.authentication.adapter.out.external.AppleTokenVerifier;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.global.security.annotation.Public;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Profile("local | dev")
@RestController
@RequestMapping("/test")
@Tag(name = "000 Test | 일반 테스트", description = "개발 및 테스트 용 API 입니다. 잘못 호출했을 떄 Dev 서버가 어떻게 되어버릴지도 몰라요")
@Slf4j
@Public
public class TestController {

    private final JwtTokenProvider jwtTokenProvider;
    private final GetChallengerUseCase getChallengerUseCase;
    private final AppleTokenVerifier appleTokenVerifier;

    @GetMapping("apple-client-secret")
    @Operation(summary = "Apple Client Secret 생성")
    String getAppleClientSecret() {
        return appleTokenVerifier.generateClientSecret();
    }

    @Public
    @Operation(summary = "AccessToken 발급")
    @GetMapping("/token/access")
    public String getAccessToken(
        @RequestParam Long memberId,
        @RequestParam(required = false) Long expirationInMinutes
    ) {
        return expirationInMinutes == null ?
            // null이면 기본값
            jwtTokenProvider.createAccessToken(memberId, null) :
            // 받았으면 해당 시간으로 만료하기
            jwtTokenProvider.createAccessToken(memberId, null, expirationInMinutes * 60);
    }

    @Operation(summary = "RefreshToken 발급")
    @Public
    @GetMapping("/token/refresh")
    public String getRefreshToken(@RequestParam Long memberId) {
        return jwtTokenProvider.createRefreshToken(memberId);
    }

    @Operation(summary = "EmailVerificationToken 발급")
    @Public
    @GetMapping("/token/email")
    public String getEmailVerification(@RequestParam String email) {
        return jwtTokenProvider.createEmailVerificationToken(email);
    }

    @Operation(summary = "oAuthVerificationToken 발급")
    @Public
    @GetMapping("/token/oauth")
    public String getOAuthVerificationToken(OAuthProvider provider, String providerId, String email) {
        return jwtTokenProvider.createOAuthVerificationToken(email, provider, providerId);
    }


    @Operation(summary = "헬스 체크 API")
    @Public
    @GetMapping("/health-check")
    public String healthCheck() {
        return "OK";
    }

    @Operation(summary = "인증된 사용자인지 여부를 확인합니다.", description = "인증되지 않은 사용자인 경우 401을 반환합니다.")
    @GetMapping("/check-authenticated")
    public ApiResponse<String> checkAuthenticated(@CurrentMember MemberPrincipal currentUser) {
        return ApiResponse.onSuccess(currentUser.toString());
    }

    @GetMapping("log-test")
    public void logTest() {
        log.trace("TRACE");
        log.debug("DEBUG");
        log.info("INFO");
        log.warn("WARN");
        log.error("ERROR");
    }
}
