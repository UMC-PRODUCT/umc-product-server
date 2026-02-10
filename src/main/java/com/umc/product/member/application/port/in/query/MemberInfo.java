package com.umc.product.member.application.port.in.query;

import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.domain.Member;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record MemberInfo(
    Long id,
    String name,
    String nickname,
    String email,
    Long schoolId,
    String schoolName,
    String profileImageId,
    String profileImageLink,
    MemberStatus status
) {
    @Deprecated
    public static MemberInfo from(Member member) {
        log.error("학교명과 프로필 이미지 링크를 포함하지 않는 생성자를 사용 중에 있습니다.");

        return new MemberInfo(
            member.getId(),
            member.getName(),
            member.getNickname(),
            member.getEmail(),
            member.getSchoolId(),
            "알 수 없음 ",
            member.getProfileImageId(),
            null,
            member.getStatus()
        );
    }

    public static MemberInfo from(Member member, String schoolName, String profileImageLink) {
        return new MemberInfo(
            member.getId(),
            member.getName(),
            member.getNickname(),
            member.getEmail(),
            member.getSchoolId(),
            schoolName,
            member.getProfileImageId(),
            profileImageLink,
            member.getStatus()
        );
    }
}
