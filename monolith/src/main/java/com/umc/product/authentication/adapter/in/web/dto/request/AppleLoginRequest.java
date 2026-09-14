package com.umc.product.authentication.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ClientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AppleLoginRequest(
    // Apple 로그인은 ID Token 대신 Authorization Code를 이용함.
    // 추후 revoke를 위해 필요한 refresh token은 ID Token으로는 발급이 불가능하기 때문이며,
    // 자세한 사항은 Apple Developer 문서를 참고해주세요.
    // https://developer.apple.com/documentation/signinwithapplerestapi/tokenresponse
    // https://developer.apple.com/documentation/signinwithapplerestapi/revoke-tokens
    @NotBlank String authorizationCode,

    // 클라이언트 플랫폼. Apple은 플랫폼별로 다른 client_id(Bundle ID vs Services ID)를 사용하므로,
    // authorization code 교환 시 정확한 client_id를 매칭하기 위해 필요함.
    @NotNull ClientType clientType
) {
}