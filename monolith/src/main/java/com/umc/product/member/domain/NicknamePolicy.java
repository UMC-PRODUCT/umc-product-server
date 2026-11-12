package com.umc.product.member.domain;

import java.util.regex.Pattern;

import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;

public final class NicknamePolicy {

    public static final int MAX_LENGTH = 4;
    private static final Pattern VALID_NICKNAME = Pattern.compile("^[가-힣]{1,4}$");

    private NicknamePolicy() {
    }

    public static void validate(String nickname) {
        if (nickname == null || !VALID_NICKNAME.matcher(nickname).matches()) {
            throw new MemberDomainException(MemberErrorCode.INVALID_NICKNAME);
        }
    }
}
