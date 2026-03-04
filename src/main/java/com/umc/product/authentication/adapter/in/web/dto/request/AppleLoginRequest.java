package com.umc.product.authentication.adapter.in.web.dto.request;

public record AppleLoginRequest(
    // Apple 로그인은 ID Token 대신 Authorization Code를 이용함.
    // 추후 revoke를 위해 필요한 refresh token은 ID Token으로는 발급이 불가능하기 때문이며,
    // 자세한 사항은 Apple Developer 문서를 참고해주세요.
    // https://developer.apple.com/documentation/signinwithapplerestapi/tokenresponse
    // https://developer.apple.com/documentation/signinwithapplerestapi/revoke-tokens
    String authorizationCode
) {
}
