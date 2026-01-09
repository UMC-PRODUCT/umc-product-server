package com.umc.product.member.application.port.in.command;

import static java.util.Objects.requireNonNull;

import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberOAuth;
import com.umc.product.member.domain.MemberTermAgreement;
import com.umc.product.member.domain.OAuthProvider;
import com.umc.product.member.domain.enums.TermType;
import java.util.List;

public record RegisterMemberCommand(
        String name,
        String nickname,
        String email,
        OAuthProvider oauthProvider,
        String providerId,
        List<TermType> agreedTerms
) {
    public RegisterMemberCommand {
        requireNonNull(name, "이름은 null일 수 없습니다.");
        requireNonNull(nickname, "닉네임은 null일 수 없습니다.");
        requireNonNull(email, "이메일은 null일 수 없습니다.");
        requireNonNull(oauthProvider, "OAuth 제공자는 null일 수 없습니다.");
        requireNonNull(providerId, "OAuth 제공자 측 사용자 ID는 null일 수 없습니다.");
        requireNonNull(agreedTerms, "약관 동의 여부는 null일 수 없습니다.");
    }

    public Member toMemberEntity() {
        return Member.builder()
                .name(name)
                .nickname(nickname)
                .email(email)
                .build();
    }

    public MemberOAuth toMemberOAuthEntity(Long memberId) {
        return MemberOAuth.builder()
                .memberId(memberId)
                .provider(oauthProvider)
                .providerId(providerId)
                .build();
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
