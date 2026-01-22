package com.umc.product.member.adapter.in.web.dto.request;

import java.util.List;

public record RegisterMemberRequest(
        String oAuthVerificationToken,
        String name,
        String nickname,
        String emailVerificationToken,
        Long schoolId,
        Long profileImageId,
        List<TermConsentStatus> termsAgreements
) {
}
