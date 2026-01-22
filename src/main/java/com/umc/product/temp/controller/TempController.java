package com.umc.product.temp.controller;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.global.security.annotation.Public;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Profile("local | dev")
@RestController("temp")
@Tag(name = Constants.TEST)
public class TempController {

    private final JwtTokenProvider jwtTokenProvider;
    private final GetChallengerUseCase getChallengerUseCase;

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
    public ApiResponse<String> getAccessToken(@PathVariable Long memberId) {
        return ApiResponse.onSuccess(jwtTokenProvider.createAccessToken(memberId, null));
    }

    @Operation(summary = "RefreshToken 발급")
    @Public
    @GetMapping("/token/refresh/{memberId}")
    public ApiResponse<String> getRefreshToken(@PathVariable Long memberId) {
        return ApiResponse.onSuccess(jwtTokenProvider.createRefreshToken(memberId));
    }

    @Operation(summary = "EmailVerification Token 발급")
    @Public
    @GetMapping("/token/email/{email}")
    public ApiResponse<String> getEmailVerification(@PathVariable String email) {
        return ApiResponse.onSuccess(jwtTokenProvider.createEmailVerificationToken(email));
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
}
