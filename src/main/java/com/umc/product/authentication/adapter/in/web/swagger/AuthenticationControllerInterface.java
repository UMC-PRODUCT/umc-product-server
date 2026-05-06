package com.umc.product.authentication.adapter.in.web.swagger;

import com.umc.product.authentication.adapter.in.web.dto.request.AppleLoginRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.GoogleLoginRequest;
import com.umc.product.authentication.adapter.in.web.dto.request.KakaoLoginRequest;
import com.umc.product.authentication.adapter.in.web.dto.response.OAuthLoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Authentication | 로그인")
public interface AuthenticationControllerInterface {
    @Operation(summary = "[LOGIN-001] Google 로그인",
        description = """
            Web에서 Redirect 방식으로 사용하려면 아래의 Link를 참고해주세요.

            [Local](http://localhost:8080/api/v1/auth/oauth2/authorization/google)
            [Development](https://dev.api.umc.it.kr/api/v1/auth/oauth2/authorization/google)
            [Production](https://api.umc.it.kr/api/v1/auth/oauth2/authorization/google)

            Google 측에서 제공받은 idToken을 통해 서버가 진위여부를 판단할 수 있도록 해야 합니다.

            **응답 설명:**
            - `success=true, code=LOGIN_SUCCESS`: 기존 회원 로그인 성공. accessToken, refreshToken 발급됨.
            - `success=true, code=REGISTER_REQUIRED`: OAuth 인증 성공, 회원가입 필요. oAuthVerificationToken 발급됨.
            """)
    OAuthLoginResponse googleOAuthLogin(
        @RequestBody GoogleLoginRequest request
    );

    @Operation(summary = "[LOGIN-002] Kakao 로그인",
        description = """
            Web에서 Redirect 방식으로 사용하려면 아래의 Link를 참고해주세요.

            [Local](http://localhost:8080/api/v1/auth/oauth2/authorization/kakao)
            [Development](https://dev.api.umc.it.kr/api/v1/auth/oauth2/authorization/kakao)
            [Production](https://api.umc.it.kr/api/v1/auth/oauth2/authorization/kakao)

            Kakao 측에서 제공받은 accessToken을 통해 서버가 진위여부를 판단할 수 있도록 해야 합니다.

            **응답 설명:**
            - `success=true, code=LOGIN_SUCCESS`: 기존 회원 로그인 성공. accessToken, refreshToken 발급됨.
            - `success=true, code=REGISTER_REQUIRED`: OAuth 인증 성공, 회원가입 필요. oAuthVerificationToken 발급됨.
            """)
    OAuthLoginResponse kakaoOAuthLogin(
        @RequestBody KakaoLoginRequest request
    );

    @Operation(summary = "[LOGIN-003] Apple 로그인",
        description = """
            Web에서 Redirect 방식으로 사용하려면 아래의 Link를 참고해주세요.

            [Local](http://localhost:8080/api/v1/auth/oauth2/authorization/apple)
            [Development](https://dev.api.umc.it.kr/api/v1/auth/oauth2/authorization/apple)
            [Production](https://api.umc.it.kr/api/v1/auth/oauth2/authorization/apple)

            Apple 측에서 받은 authorization code와 함께 클라이언트 플랫폼(`clientType`)을 전달해주세요.
            Apple은 플랫폼별로 서로 다른 client_id(iOS Bundle ID vs Web Services ID)를 사용하므로
            `clientType`(ANDROID, IOS, WEB)을 정확히 명시해야 토큰 교환이 가능합니다.
            """)
    OAuthLoginResponse appleOAuthLogin(
        @RequestBody AppleLoginRequest request
    );
}
