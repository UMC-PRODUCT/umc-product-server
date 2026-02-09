package com.umc.product.test.controller;

import com.umc.product.authentication.adapter.out.external.AppleTokenVerifier;
import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.global.constant.SwaggerTag.Constants;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Profile("local | dev")
@RestController
@RequestMapping("/test")
@Tag(name = Constants.TEST)
@Slf4j
public class TestController {

    private final JwtTokenProvider jwtTokenProvider;
    private final GetChallengerUseCase getChallengerUseCase;
    private final AppleTokenVerifier appleTokenVerifier;

    @Public
    @GetMapping("apple-client-secret")
    String getAppleClientSecret() {
        return appleTokenVerifier.generateClientSecret();
    }

    @GetMapping("permission/notice-read")
    @CheckAccess(
        resourceType = ResourceType.NOTICE,
        resourceId = "#noticeId", // SpEL 표현식 - 공부하세요!!
        permission = PermissionType.READ,
        message = "하나야 스트레스 많이 받을거야~ 자기 전에도 생각 날꺼야~ 도움 많이 될꺼야~"
    )
    void permissionTest(Long noticeId) {
    }

    @GetMapping("permission/no-evaluator-test")
    @CheckAccess(
        resourceType = ResourceType.CURRICULUM,
        resourceId = "#noticeId",
        permission = PermissionType.DELETE,
        message = "하나야 스트레스 많이 받을거야~ 자기 전에도 생각 날꺼야~ 도움 많이 될꺼야~"
    )
    void noEvaluatorForPermission(Long something) {
    }

    @GetMapping("challenger")
    @Operation(summary = "memberId와 gisuId로 챌린저 정보 조회")
    public ChallengerInfo getChallengerByMemberAndGisuId(
        Long memberId, Long gisuId
    ) {
        return getChallengerUseCase.getByMemberIdAndGisuId(
            memberId, gisuId
        );
    }

    @Operation(summary = "AccessToken 발급")
    @Public
    @GetMapping("/token/access/{memberId}")
    public String getAccessToken(@PathVariable Long memberId) {
        return jwtTokenProvider.createAccessToken(memberId, null);
    }

    @Operation(summary = "RefreshToken 발급")
    @Public
    @GetMapping("/token/refresh/{memberId}")
    public String getRefreshToken(@PathVariable Long memberId) {
        return jwtTokenProvider.createRefreshToken(memberId);
    }

    @Operation(summary = "EmailVerificationToken 발급")
    @Public
    @GetMapping("/token/email/{email}")
    public String getEmailVerification(@PathVariable String email) {
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
