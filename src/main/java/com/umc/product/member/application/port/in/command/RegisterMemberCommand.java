package com.umc.product.member.application.port.in.command;

import static java.util.Objects.requireNonNull;

import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberOAuth;
import com.umc.product.member.domain.MemberTermAgreement;
import com.umc.product.member.domain.OAuthProvider;
import com.umc.product.member.domain.TermType;
import java.util.List;

public record RegisterMemberCommand(
        String name,
        String nickname,
        String email,
        OAuthProvider oauthProvider,
        String providerUserId,
        List<TermType> agreedTerms
) {
    public RegisterMemberCommand {
        requireNonNull(name, "name must not be null");
        requireNonNull(nickname, "nickname must not be null");
        requireNonNull(email, "email must not be null");
        requireNonNull(oauthProvider, "oauthProvider must not be null");
        requireNonNull(providerUserId, "providerUserId must not be null");
        requireNonNull(agreedTerms, "agreedTerms must not be null");
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
                .providerUserId(providerUserId)
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
