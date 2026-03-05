package com.umc.product.member.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 탈퇴 요청")
public record DeleteMemberRequest(
    @Schema(description = "Google Access Token (Google OAuth 연동 해제 시 필요)")
    String googleAccessToken,

    @Schema(description = "Kakao Access Token (Kakao OAuth 연동 해제 시 필요)")
    String kakaoAccessToken
) {
}
