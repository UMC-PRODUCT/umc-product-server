package com.umc.product.member.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "회원가입 요청")
public record RegisterMemberRequest(

        @Schema(description = "OAuth 인증 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @NotBlank(message = "OAuth 인증 토큰은 필수입니다")
        String oAuthVerificationToken,

        @Schema(description = "이름", example = "홍길동")
        @NotBlank(message = "이름은 필수입니다")
        @Size(max = 30, message = "이름은 30자 (한글의 경우 10자) 이하여야 합니다")
        String name,

        @Schema(description = "닉네임", example = "닉넴")
        @NotBlank(message = "닉네임은 필수입니다")
        @Size(min = 1, max = 20, message = "닉네임은 한글 1~5자여야 합니다")
        @Pattern(regexp = "^[가-힣]+$", message = "한글만 입력 가능합니다")
        String nickname,

        @Schema(description = "이메일 인증 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @NotBlank(message = "이메일 인증 토큰은 필수입니다")
        String emailVerificationToken,

        @Schema(description = "학교 ID", example = "1")
        @NotNull(message = "학교 ID는 필수입니다")
        Long schoolId,

        @Schema(description = "프로필 이미지 ID (선택)", example = "1")
        Long profileImageId,

        @Schema(description = "약관 동의 목록")
        @NotNull(message = "약관 동의 목록은 필수입니다")
        @Size(min = 1, message = "최소 1개 이상의 약관 동의 정보가 필요합니다")
        @Valid
        List<TermConsentStatus> termsAgreements
) {
}
