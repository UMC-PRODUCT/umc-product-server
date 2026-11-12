package com.umc.product.member.application.port.in.query.dto;

import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.domain.Member;

public record MemberLoginAccountInfo(Long memberId, String email, MemberStatus status, boolean hasPassword) {

    public static MemberLoginAccountInfo from(Member member) {
        return new MemberLoginAccountInfo(member.getId(), member.getEmail(), member.getStatus(),
            member.hasCredential());
    }
}
