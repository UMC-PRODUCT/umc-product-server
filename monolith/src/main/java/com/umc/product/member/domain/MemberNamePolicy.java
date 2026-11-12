package com.umc.product.member.domain;

import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;

public final class MemberNamePolicy {

    public static final int MAX_LENGTH = 5;

    private MemberNamePolicy() {
    }

    public static void validate(String name) {
        if (name == null || name.isBlank() || name.length() > MAX_LENGTH) {
            throw new MemberDomainException(MemberErrorCode.INVALID_NAME);
        }
    }
}
