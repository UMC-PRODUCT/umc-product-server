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
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthenticationControllerInterface {
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
    );

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
    OAuthLoginResponse kakaoOAuthLogin(
        @RequestBody KakaoLoginRequest request
    );

    @Operation(summary = "Apple 로그인",
        description = """
            Web에서 Redirect 방식으로 사용하려면 아래의 Link를 참고해주세요.

            [Local](http://localhost:8080/api/v1/auth/oauth2/authorization/apple)
            [Development](https://dev.umc-product.kyeoungwoon.kr/api/v1/auth/oauth2/authorization/apple)
            [Production](https://umc-product.kyeoungwoon.kr/api/v1/auth/oauth2/authorization/apple)

            Apple 로그인은 제옹과 협의 후에 구현 예정입니다.
            """)
    OAuthLoginResponse appleOAuthLogin(
        @RequestBody AppleLoginRequest request
    );

    @Operation(summary = "6자리 인증코드로 이메일 인증",
        description = """
            이메일로 발송된 인증코드를 통해서 이메일 인증을 완료합니다.

            emailVerificationToken을 발급하며, 해당 토큰을 회원가입 시에 제공해야 합니다.
            """)
    CompleteEmailVerificationResponse verifyEmailByCode(
        @RequestBody CompleteEmailVerificationRequest request
    );

    @Operation(summary = "이메일 인증 코드 발송",
        description = """
            인증을 요청하는 이메일로 인증 코드를 발송합니다.

            이메일 인증코드는 6자리의 숫자로만 구성되어 있습니다.
            """)
    SendEmailVerificationResponse sendEmailVerification(
        @RequestBody SendEmailVerificationRequest request
    );

    @Operation(summary = "AccessToken 재발급",
        description = """
            RefreshToken을 이용해서 AccessToken을 재발급합니다.
            """)
    RenewAccessTokenResponse renewAccessToken(
        @RequestBody RenewAccessTokenRequest request
    );
}
