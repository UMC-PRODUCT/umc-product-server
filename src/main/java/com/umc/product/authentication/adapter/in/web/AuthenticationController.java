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
import com.umc.product.authentication.application.port.in.command.dto.IdTokenLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.IdTokenLoginResult;
import com.umc.product.authentication.application.port.in.command.dto.ValidateEmailVerificationSessionCommand;
import com.umc.product.authentication.domain.enums.OAuth2ResultCode;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.annotation.Public;
import io.swagger.v3.oas.annotations.Operation;
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
public class AuthenticationController {

    private final ManageAuthenticationUseCase manageAuthenticationUseCase;
    private final OAuthAuthenticationUseCase oAuthAuthenticationUseCase;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("login/google")
    @Public
    @Operation(summary = "Google 로그인",
            description = """
                    Web에서 Redirect 방식으로 사용하려면 아래의 Link를 참고해주세요.
                    
                    [Local](http://localhost:8080/api/v1/auth/oauth2/authorization/google)
                    [Development](https://dev.umc-product.kyeoungwoon.kr/api/v1/auth/oauth2/authorization/google)
                    [Production](https://umc-product.kyeoungwoon.kr/api/v1/auth/oauth2/authorization/google)
                    
                    Google 측에서 제공받은 idToken을 통해 서버가 진위여부를 판단할 수 있도록 해야 합니다.
                    
                    **응답 설명:**
                    - `success=true, code=LOGIN_SUCCESS`: 기존 회원 로그인 성공. accessToken, refreshToken 발급됨.
                    - `success=true, code=REGISTER_REQUIRED`: OAuth 인증 성공, 회원가입 필요. oAuthVerificationToken 발급됨.
                    """)
    OAuthLoginResponse googleOAuthLogin(
            @RequestBody GoogleLoginRequest request
    ) {
        return processIdTokenLogin(OAuthProvider.GOOGLE, request.idToken());
    }

    @Public
    @Operation(summary = "Kakao 로그인",
            description = """
                    Web에서 Redirect 방식으로 사용하려면 아래의 Link를 참고해주세요.
                    
                    [Local](http://localhost:8080/api/v1/auth/oauth2/authorization/kakao)
                    [Development](https://dev.umc-product.kyeoungwoon.kr/api/v1/auth/oauth2/authorization/kakao)
                    [Production](https://umc-product.kyeoungwoon.kr/api/v1/auth/oauth2/authorization/kakao)
                    
                    Kakao 측에서 제공받은 accessToken을 통해 서버가 진위여부를 판단할 수 있도록 해야 합니다.
                    
                    **응답 설명:**
                    - `success=true, code=LOGIN_SUCCESS`: 기존 회원 로그인 성공. accessToken, refreshToken 발급됨.
                    - `success=true, code=REGISTER_REQUIRED`: OAuth 인증 성공, 회원가입 필요. oAuthVerificationToken 발급됨.
                    """)
    @PostMapping("login/kakao")
    OAuthLoginResponse kakaoOAuthLogin(
            @RequestBody KakaoLoginRequest request
    ) {
        return processIdTokenLogin(OAuthProvider.KAKAO, request.accessToken());
    }

    /**
     * ID 토큰(또는 Access Token) 기반 OAuth 로그인 처리
     */
    private OAuthLoginResponse processIdTokenLogin(OAuthProvider provider, String token) {
        IdTokenLoginResult result = oAuthAuthenticationUseCase.idTokenLogin(
                new IdTokenLoginCommand(provider, token)
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

    @Public
    @Operation(summary = "Apple 로그인",
            description = """  
                    Web에서 Redirect 방식으로 사용하려면 아래의 Link를 참고해주세요.
                    
                    [Local](http://localhost:8080/api/v1/auth/oauth2/authorization/apple)
                    [Development](https://dev.umc-product.kyeoungwoon.kr/api/v1/auth/oauth2/authorization/apple)
                    [Production](https://umc-product.kyeoungwoon.kr/api/v1/auth/oauth2/authorization/apple)
                    
                    Apple 로그인은 제옹과 협의 후에 구현 예정입니다.
                    """)
    @PostMapping("login/apple")
    OAuthLoginResponse appleOAuthLogin(
            @RequestBody AppleLoginRequest request
    ) {
        throw new NotImplementedException();
    }

    @Operation(summary = "6자리 인증코드로 이메일 인증",
            description = """
                    이메일로 발송된 인증코드를 통해서 이메일 인증을 완료합니다.
                    
                    emailVerificationToken을 발급하며, 해당 토큰을 회원가입 시에 제공해야 합니다.
                    """)
    @PostMapping("email-verification/code")
    @Public
    CompleteEmailVerificationResponse verifyEmailByCode(
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

    @Operation(summary = "이메일 인증 코드 발송",
            description = """
                    인증을 요청하는 이메일로 인증 코드를 발송합니다.
                    
                    이메일 인증코드는 6자리의 숫자로만 구성되어 있습니다.
                    """)
    @PostMapping("email-verification")
    @Public
    SendEmailVerificationResponse sendEmailVerification(
            @RequestBody SendEmailVerificationRequest request
    ) {
        Long sessionId = manageAuthenticationUseCase.createEmailVerificationSession(request.email());

        return SendEmailVerificationResponse
                .builder()
                .emailVerificationId(sessionId.toString())
                .build();
    }

    @Operation(summary = "AccessToken 재발급",
            description = """
                    RefreshToken을 이용해서 AccessToken을 재발급합니다.
                    """)
    @PostMapping("token/renew")
    RenewAccessTokenResponse renewAccessToken(
            @RequestBody RenewAccessTokenRequest request
    ) {
        String newAccessToken = manageAuthenticationUseCase.renewAccessToken(
                request.toCommand()
        );

        return RenewAccessTokenResponse
                .builder()
                .accessToken(newAccessToken)
                .build();
    }
}
