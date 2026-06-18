package com.umc.product.member.application.port.in.query.dto;

import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import java.util.List;
import lombok.Builder;

/**
 * 회원에 대한 모든 정보를 담고 있는 DTO
 */
@Builder
public record MemberInfo(
    Long id,
    String name,
    String nickname,
    String email,
    Long schoolId,
    String schoolName,
    String profileImageId,
    String profileImageLink,
    MemberStatus status,
    List<ChallengerRoleInfo> roles
) {
    @Deprecated
    public static MemberInfo from(Member member) {
        return new MemberInfo(
            member.getId(),
            member.getName(),
            member.getNickname(),
            member.getEmail(),
            member.getSchoolId(),
            "알 수 없음 ",
            member.getProfileImageId(),
            null,
            member.getStatus(),
            null
        );
    }

    @Deprecated
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
            member.getStatus(),
            null
        );
    }

    public static MemberInfo from(
        Member member, String schoolName, String profileImageLink, List<ChallengerRoleInfo> roles
    ) {
        return MemberInfo.builder()
            .id(member.getId())
            .name(member.getName())
            .nickname(member.getNickname())
            .email(member.getEmail())
            .schoolId(member.getSchoolId())
            .schoolName(schoolName)
            .profileImageId(member.getProfileImageId())
            .profileImageLink(profileImageLink)
            .status(member.getStatus())
            .roles(roles)
            .build();
    }

    public void validateHasSchool() {
        if (schoolId == null) {
            throw new MemberDomainException(MemberErrorCode.MEMBER_SCHOOL_NOT_ASSIGNED);
        }
    }

    public MemberInfo toPublic() {
        return MemberInfo.builder()
            .id(this.id)
            .name(this.name)
            .nickname(this.nickname)
            .email("알 수 없음")
            .schoolId(this.schoolId)
            .schoolName(this.schoolName)
            .profileImageId(this.profileImageId)
            .profileImageLink(this.profileImageLink)
            .status(this.status)
            .roles(this.roles)
            .build();
    }
}
