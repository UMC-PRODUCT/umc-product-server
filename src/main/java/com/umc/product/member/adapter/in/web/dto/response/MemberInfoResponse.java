package com.umc.product.member.adapter.in.web.dto.response;

import com.umc.product.authorization.application.port.in.query.ChallengerRoleInfo;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.member.application.port.in.query.MemberProfileInfo;
import java.util.List;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 정보를 응답하는 DTO 입니다.
 * <p>
 * 현재는 내 프로필 조회와 남의 프로필 조회를 동일한 Response를 사용하고 있는데,
 * <p>
 * 추후 Public/Private 정보를 구분해서 사용해야 할 경우 분리가 필요합니다.
 */
@Builder
@Slf4j
public record MemberInfoResponse(
    Long id,
    String name,
    String nickname,
    String email,
    Long schoolId,
    String schoolName,
    String profileImageLink,
    MemberStatus status,
    List<ChallengerRoleInfo> roles, // TODO: 이거 그대로 써도 괜찮은거 맞나?
    List<ChallengerInfoResponse> challengerRecords
    // TODO: 활동 이력이나 각종 링크들 추가해야 함
) {
    @Deprecated
    public static MemberInfoResponse from(MemberProfileInfo info) {
        log.error("MemberProfileInfo is Deprecated. Use MemberInfo to create MemberInfoResponse");

        return new MemberInfoResponse(
            info.id(),
            info.name(),
            info.nickname(),
            info.email(),
            info.schoolId(),
            info.schoolName(),
            info.profileImageLink(),
            info.status(),
            info.roles(),
            null
        );
    }

    public static MemberInfoResponse from(MemberInfo info, List<ChallengerInfoResponse> challengerRecords) {
        return new MemberInfoResponse(
            info.id(),
            info.name(),
            info.nickname(),
            info.email(),
            info.schoolId(),
            info.schoolName(),
            info.profileImageLink(),
            info.status(),
            info.roles(),
            challengerRecords
        );
    }
}
