package com.umc.product.member.adapter.in.web.dto.request;

import com.umc.product.member.application.port.in.command.CompleteRegisterMemberCommand;
import com.umc.product.member.domain.enums.TermType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 회원가입 완료 요청 DTO
 */
public record CompleteRegisterMemberRequest(
        @NotBlank(message = "이름은 필수입니다")
        String name,

        @NotBlank(message = "닉네임은 필수입니다")
        String nickname,

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "유효한 이메일 형식이어야 합니다")
        String email,

        Long schoolId,
        Long profileImageId,

        @NotNull(message = "약관 동의는 필수입니다")
        List<TermType> agreedTerms
) {
    public CompleteRegisterMemberCommand toCommand(Long memberId) {
        return CompleteRegisterMemberCommand.builder()
                .memberId(memberId)
                .name(name)
                .nickname(nickname)
                .email(email)
                .schoolId(schoolId)
                .profileImageId(profileImageId)
                .agreedTerms(agreedTerms)
                .build();
    }
}
