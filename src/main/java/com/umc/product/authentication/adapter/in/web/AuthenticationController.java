package com.umc.product.authentication.adapter.in.web;

import com.umc.product.authentication.adapter.in.web.dto.request.AppleLoginRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.GoogleLoginRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.KakaoCodeLoginRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.KakaoLoginRequest;
import com.umc.product.authentication.adapter.in.web.dto.response.OAuthLoginResponse;
import com.umc.product.authentication.adapter.in.web.swagger.AuthenticationControllerInterface;
import com.umc.product.authentication.application.port.in.command.OAuthAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.AccessTokenLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.AuthorizationCodeLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.OAuthTokenLoginResult;
import com.umc.product.authentication.application.port.out.AppleAuthorizationCodeResult;
import com.umc.product.authentication.application.port.out.VerifyOAuthTokenPort;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.annotation.Public;
import jakarta.validation.Valid;
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
    @PostMapping("login/kakao/code")
    @Public
    public OAuthLoginResponse kakaoOAuthCodeLogin(
        @Valid @RequestBody KakaoCodeLoginRequest request
    ) {
        OAuthTokenLoginResult result = oAuthAuthenticationUseCase.authorizationCodeLogin(
            new AuthorizationCodeLoginCommand(
                OAuthProvider.KAKAO,
                request.authorizationCode(),
                request.redirectUri()
            )
        );

        return buildLoginResponse(OAuthProvider.KAKAO, result);
    }

    @Override
    @Public
    @PostMapping("login/apple")
    public OAuthLoginResponse appleOAuthLogin(
        @RequestBody AppleLoginRequest request
    ) {
        // Authorization Code 방식. Apple은 플랫폼별로 다른 client_id를 사용하므로 clientType을 함께 전달한다.
        AppleAuthorizationCodeResult codeResult = verifyOAuthTokenPort.verifyAppleAuthorizationCode(
            request.authorizationCode(), request.clientType()
        );
        OAuthTokenLoginResult result = oAuthAuthenticationUseCase.loginWithOAuth2Attributes(codeResult.attrs());

        if (result.isExistingMember() && codeResult.refreshToken() != null) {
            // 기존 회원: MemberOAuth에 appleRefreshToken과 appleClientId 갱신
            oAuthAuthenticationUseCase.updateAppleRefreshToken(
                OAuthProvider.APPLE, result.providerId(), codeResult.refreshToken(), codeResult.clientId()
            );
        }

        return buildLoginResponse(OAuthProvider.APPLE, result, codeResult.refreshToken());
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
        return buildLoginResponse(provider, result, null);
    }

    private OAuthLoginResponse buildLoginResponse(OAuthProvider provider, OAuthTokenLoginResult result,
                                                  String appleRefreshToken) {
        if (result.isExistingMember()) {
            // 기존 회원: JWT 발급
            String accessToken = jwtTokenProvider.createAccessToken(
                result.memberId(),
                Collections.emptyList()
            );
            String refreshToken = jwtTokenProvider.createRefreshToken(result.memberId());

            return OAuthLoginResponse.ofLoginSuccess(
                provider, accessToken, refreshToken
            );
        } else {
            // 신규 회원: oAuthVerificationToken 발급 (회원가입 시 사용)
            String oAuthVerificationToken = jwtTokenProvider.createOAuthVerificationToken(
                result.email(),
                result.provider(),
                result.providerId()
            );

            return OAuthLoginResponse.ofRegisterRequired(
                provider, oAuthVerificationToken
            );
        }
    }
}
