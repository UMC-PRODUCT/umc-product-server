package com.umc.product.member.application.port.in.command;

import static java.util.Objects.requireNonNull;

import com.umc.product.member.domain.MemberTermAgreement;
import com.umc.product.member.domain.enums.TermType;
import java.util.List;
import lombok.Builder;

/**
 * 회원가입 완료를 위한 Command
 */
@Builder
public record CompleteRegisterMemberCommand(
        Long memberId, // 가입 완료할 member의 ID
        String name,
        String nickname,
        String email,
        Long schoolId,
        Long profileImageId,
        List<TermType> agreedTerms
) {
    public CompleteRegisterMemberCommand {
        requireNonNull(memberId, "회원 ID는 null일 수 없습니다.");
        requireNonNull(name, "이름은 null일 수 없습니다.");
        requireNonNull(nickname, "닉네임은 null일 수 없습니다.");
        requireNonNull(email, "이메일은 null일 수 없습니다.");
        requireNonNull(schoolId, "학교 ID는 null일 수 없습니다.");
        requireNonNull(agreedTerms, "약관 동의 여부는 null일 수 없습니다.");
    }

    public List<MemberTermAgreement> toTermAgreementEntities(Long memberId) {
        return agreedTerms.stream()
                .map(termType -> MemberTermAgreement.builder()
                        .memberId(memberId)
                        .termType(termType)
                        .build())
                .toList();
    }
}
