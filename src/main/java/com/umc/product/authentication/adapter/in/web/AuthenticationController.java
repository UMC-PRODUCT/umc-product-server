package com.umc.product.authentication.adapter.in.web;

import com.umc.product.authentication.adapter.in.web.dto.request.AppleLoginRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.CompleteEmailVerificationRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.GoogleLoginRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.KakaoLoginRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.RenewAccessTokenRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.SendEmailVerificationRequest;
import com.umc.product.authentication.adapter.in.web.dto.response.CompleteEmailVerificationResponse;
import com.umc.product.authentication.adapter.in.web.dto.response.OAuthLoginResponse;
import com.umc.product.authentication.adapter.in.web.dto.response.RenewAccessTokenResponse;
import com.umc.product.authentication.adapter.in.web.dto.response.SendEmailVerificationResponse;
import com.umc.product.authentication.application.port.in.command.ManageAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.OAuthAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.AccessTokenLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.OAuthTokenLoginResult;
import com.umc.product.authentication.application.port.in.command.dto.ValidateEmailVerificationSessionCommand;
import com.umc.product.authentication.domain.enums.OAuth2ResultCode;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.annotation.Public;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = Constants.AUTH)
public class AuthenticationController implements AuthenticationControllerInterface {

    private final ManageAuthenticationUseCase manageAuthenticationUseCase;
    private final OAuthAuthenticationUseCase oAuthAuthenticationUseCase;
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
        throw new NotImplementedException();
    }

    @Override
    @PostMapping("email-verification/code")
    @Public
    public CompleteEmailVerificationResponse verifyEmailByCode(
        @RequestBody CompleteEmailVerificationRequest request
    ) {
        String emailVerificationToken = manageAuthenticationUseCase
            .validateEmailVerificationSession(
                ValidateEmailVerificationSessionCommand
                    .builder()
                    .sessionId(request.emailVerificationId().toString())
                    .code(request.verificationCode())
                    .build()
            );

        return CompleteEmailVerificationResponse
            .builder()
            .emailVerificationToken(emailVerificationToken)
            .build();
    }

    @Override
    @PostMapping("email-verification")
    @Public
    public SendEmailVerificationResponse sendEmailVerification(
        @RequestBody SendEmailVerificationRequest request
    ) {
        Long sessionId = manageAuthenticationUseCase.createEmailVerificationSession(request.email());

        return SendEmailVerificationResponse
            .builder()
            .emailVerificationId(sessionId.toString())
            .build();
    }

    @Override
    @PostMapping("token/renew")
    @Public
    public RenewAccessTokenResponse renewAccessToken(
        @RequestBody RenewAccessTokenRequest request
    ) {
        return RenewAccessTokenResponse.from(
            manageAuthenticationUseCase.renewAccessToken(
                request.toCommand()
            ));
    }

    /**
     * Access Token 기반 OAuth 로그인 처리
     */
    private OAuthLoginResponse processAccessTokenLogin(OAuthProvider provider, String token) {
        OAuthTokenLoginResult result = oAuthAuthenticationUseCase.accessTokenLogin(
            new AccessTokenLoginCommand(provider, token)
        );

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
