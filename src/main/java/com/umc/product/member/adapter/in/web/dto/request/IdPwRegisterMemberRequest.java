package com.umc.product.member.adapter.in.web.dto.request;

import com.umc.product.member.application.port.in.command.dto.IdPwRegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.TermConsents;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record IdPwRegisterMemberRequest(
    // id/pw에 대한 검증은 usecase 내부에서 CredentiaLPolicy를 통하기 때문에
    // Request 단에서는 추후 충돌을 방지하기 위해 최소한의 검증 (NotBlank)만 진행

    @NotBlank(message = "본 회원가입 방법에서는 ID를 필수로 제공하여야 합니다.")
    String loginId,

    @NotBlank(message = "본 회원가입 방법에서는 비밀번호를 필수로 제공하여야 합니다.")
    String rawPassword,

    @Schema(description = "이름", example = "박세은")
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 10, message = "이름은 10자 이하여야 합니다")
    String name,

    @Schema(description = "닉네임", example = "하늘")
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 1, max = 5, message = "닉네임은 한글 1~5자여야 합니다")
    @Pattern(regexp = "^[가-힣]+$", message = "한글만 입력 가능합니다")
    String nickname,

    @Schema(description = "이메일 인증 토큰", example = "some_email_token")
    @NotBlank(message = "이메일 인증 토큰은 필수입니다")
    String emailVerificationToken,

    @Schema(description = "학교 ID", example = "1")
    @NotNull(message = "학교 ID는 필수입니다")
    Long schoolId,

    @Schema(description = "약관 동의 목록")
    @NotNull(message = "약관 동의 목록은 필수입니다")
    @Size(min = 1, message = "최소 1개 이상의 약관 동의 정보가 필요합니다")
    @Valid
    List<TermConsentStatus> termsAgreements
) {
    public IdPwRegisterMemberCommand toCommand(String email, List<TermConsentStatus> termsAgreements) {
        return IdPwRegisterMemberCommand.builder()
            .loginId(loginId)
            .rawPassword(rawPassword)
            .name(name)
            .nickname(nickname)
            .email(email) // 이메일은 OAuth 회원가입 때만 받도록 변경
            .schoolId(schoolId)
            .termConsents(termsAgreements.stream().map(TermConsents::fromRequest).toList())
            .build();
    }

}
