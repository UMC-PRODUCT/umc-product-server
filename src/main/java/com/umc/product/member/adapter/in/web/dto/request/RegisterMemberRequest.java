package com.umc.product.member.adapter.in.web.dto.request;

import java.util.List;

public record RegisterMemberRequest(
        String oAuthVerificationToken,
        String name,
        String nickname,
        String emailVerificationToken,
        String schoolId,
        String profileImageId,
        List<TermsAgreement> termsAgreements
) {
    public record TermsAgreement(
            Long termsId,
            boolean isAgreed
    ) {
    }
}
