package com.umc.product.member.application.port.in.query;

import com.umc.product.authorization.application.port.in.query.ChallengerRoleInfo;
import com.umc.product.common.domain.enums.MemberStatus;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record MemberProfileInfo(
    Long id,
    String name,
    String nickname,
    String email,
    Long schoolId,
    String schoolName,
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
            profileImageLink,
            memberInfo.status(),
            null
        );
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
            profileImageLink,
            memberInfo.status(),
            roles
        );
    }

}
