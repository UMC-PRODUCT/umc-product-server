package com.umc.product.authentication.adapter.in.web;

import com.umc.product.authentication.adapter.in.oauth.OAuth2Attributes;
import com.umc.product.authentication.adapter.in.web.dto.request.AppleLoginRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.GoogleLoginRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.KakaoLoginRequest;
import com.umc.product.authentication.adapter.in.web.dto.response.OAuthLoginResponse;
import com.umc.product.authentication.adapter.in.web.swagger.AuthenticationControllerInterface;
import com.umc.product.authentication.application.port.in.command.OAuthAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.AccessTokenLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.OAuthTokenLoginResult;
import com.umc.product.authentication.application.port.out.VerifyOAuthTokenPort;
import com.umc.product.authentication.domain.enums.OAuth2ResultCode;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.annotation.Public;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController implements AuthenticationControllerInterface {

    private final OAuthAuthenticationUseCase oAuthAuthenticationUseCase;
    private final VerifyOAuthTokenPort verifyOAuthTokenPort;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @PostMapping("login/google")
    @Public
    public OAuthLoginResponse googleOAuthLogin(
        @RequestBody GoogleLoginRequest request
    ) {
        return processAccessTokenLogin(OAuthProvider.GOOGLE, request.accessToken());
    }

    @Override
    @PostMapping("login/kakao")
    @Public
    public OAuthLoginResponse kakaoOAuthLogin(
        @RequestBody KakaoLoginRequest request
    ) {
        return processAccessTokenLogin(OAuthProvider.KAKAO, request.accessToken());
    }

    @Override
    @Public
    @PostMapping("login/apple")
    public OAuthLoginResponse appleOAuthLogin(
        @RequestBody AppleLoginRequest request
    ) {
        if (request.idToken() != null) {
            return processAccessTokenLogin(OAuthProvider.APPLE, request.idToken());
        }

        // Authorization Code 방식
        OAuth2Attributes attrs = verifyOAuthTokenPort.verifyAppleAuthorizationCode(
            request.authorizationCode()
        );
        OAuthTokenLoginResult result = oAuthAuthenticationUseCase.loginWithOAuth2Attributes(attrs);
        return buildLoginResponse(OAuthProvider.APPLE, result);
    }

    /**
     * Access Token 기반 OAuth 로그인 처리
     */
    private OAuthLoginResponse processAccessTokenLogin(OAuthProvider provider, String token) {
        OAuthTokenLoginResult result = oAuthAuthenticationUseCase.accessTokenLogin(
            new AccessTokenLoginCommand(provider, token)
        );

        return buildLoginResponse(provider, result);
    }

    /**
     * OAuthTokenLoginResult를 기반으로 OAuthLoginResponse를 생성합니다.
     */
    private OAuthLoginResponse buildLoginResponse(OAuthProvider provider, OAuthTokenLoginResult result) {
        if (result.isExistingMember()) {
            // 기존 회원: JWT 발급
            String accessToken = jwtTokenProvider.createAccessToken(
                result.memberId(),
                Collections.emptyList()
            );
            String refreshToken = jwtTokenProvider.createRefreshToken(result.memberId());

            return new OAuthLoginResponse(
                provider,
                OAuth2ResultCode.SUCCESS.isSuccess(),
                OAuth2ResultCode.SUCCESS.getCode(),
                null,  // oAuthVerificationToken 불필요
                accessToken,
                refreshToken
            );
        } else {
            // 신규 회원: oAuthVerificationToken 발급 (회원가입 시 사용)
            String oAuthVerificationToken = jwtTokenProvider.createOAuthVerificationToken(
                result.email(),
                result.provider(),
                result.providerId()
            );

            return new OAuthLoginResponse(
                provider,
                OAuth2ResultCode.REGISTER_REQUIRED.isSuccess(),
                OAuth2ResultCode.REGISTER_REQUIRED.getCode(),
                oAuthVerificationToken,
                null,  // accessToken 없음
                null   // refreshToken 없음
            );
        }
    }
}
