package com.umc.product.authentication.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OAuth 연동 해제 요청")
public record UnlinkOAuthRequest(
    @Schema(description = "Google Access Token (Google OAuth 해제 시 필요)")
    String googleAccessToken,

    @Schema(description = "Kakao Access Token (Kakao OAuth 해제 시 필요)")
    String kakaoAccessToken
) {
}
