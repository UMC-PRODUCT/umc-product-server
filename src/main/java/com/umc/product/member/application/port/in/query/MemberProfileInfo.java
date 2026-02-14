package com.umc.product.member.application.port.in.query;

import com.umc.product.authorization.application.port.in.query.ChallengerRoleInfo;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.domain.Member;
import java.util.List;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

/**
 * Member Profile Info DTO
 *
 * @deprecated use {@link MemberInfo} instead, referring to Issue #391
 */
@Slf4j
@Builder
@Deprecated
public record MemberProfileInfo(
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
    /**
     * @deprecated roles 필드 추가로 인해 deprecated 되었습니다.
     */
    @Deprecated
    public static MemberProfileInfo from(MemberInfo memberInfo, String schoolName, String profileImageLink) {
        log.warn("Deprecated factory method used - 챌린저 역할 정보를 반환하지 못하고 있습니다");

        return new MemberProfileInfo(
            memberInfo.id(),
            memberInfo.name(),
            memberInfo.nickname(),
            memberInfo.email(),
            memberInfo.schoolId(),
            schoolName,
            null,
            profileImageLink,
            memberInfo.status(),
            null
        );
    }

    public static MemberProfileInfo from(
        Member member, String schoolName, String profileImageLink, List<ChallengerRoleInfo> roles
    ) {
        return MemberProfileInfo.builder()
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

    public static MemberProfileInfo from(
        MemberInfo memberInfo, String schoolName, String profileImageLink, List<ChallengerRoleInfo> roles
    ) {
        return new MemberProfileInfo(
            memberInfo.id(),
            memberInfo.name(),
            memberInfo.nickname(),
            memberInfo.email(),
            memberInfo.schoolId(),
            schoolName,
            null,
            profileImageLink,
            memberInfo.status(),
            roles
        );
    }
}
