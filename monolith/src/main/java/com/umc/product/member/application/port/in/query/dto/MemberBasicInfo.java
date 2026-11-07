package com.umc.product.member.application.port.in.query.dto;

import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.domain.Member;

public record MemberBasicInfo(
    Long memberId, String name, String nickname, Long schoolId, MemberStatus status
) {
    public static MemberBasicInfo from(Member member) {
        return new MemberBasicInfo(member.getId(), member.getName(), member.getNickname(),
            member.getSchoolId(), member.getStatus());
    }
}
