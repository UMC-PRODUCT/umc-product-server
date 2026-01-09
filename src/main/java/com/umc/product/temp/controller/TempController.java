package com.umc.product.temp.controller;

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

    @Operation(summary = "테스트용 토큰 발급", description = "userId로 JWT 토큰을 발급합니다.")
    @Public
    @GetMapping("/token/at/{userId}")
    public ApiResponse<String> getTestToken(@PathVariable Long userId) {
        return ApiResponse.onSuccess(jwtTokenProvider.createAccessToken(userId, null));
    }

    @Operation(summary = "헬스 체크 API")
    @Public
    @GetMapping("/health-check")
    public String healthCheck() {
        return "OK";
    }

    @Operation(summary = "인증된 사용자인지 여부를 확인합니다.", description = "인증되지 않은 사용자인 경우 401을 반환합니다.")
    @GetMapping("/check-authenticated")
    public String checkAuthenticated(@CurrentMember MemberPrincipal currentUser) {
        return currentUser.toString();
    }
}
