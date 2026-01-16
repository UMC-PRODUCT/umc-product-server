package com.umc.product.member.adapter.in.web.dto.response;

import com.umc.product.member.domain.enums.MemberStatus;

/**
 * 사용자 정보를 응답하는 DTO 입니다.
 * <p>
 * 현재는 내 프로필 조회와 남의 프로필 조회를 동일한 Response를 사용하고 있는데,
 * <p>
 * 추후 Public/Private 정보를 구분해서 사용해야 할 경우 분리가 필요합니다.
 */
public record MemberInfoResponse(
        Long id,
        String name,
        String nickname,
        String email,
        Long schoolId,
        Long schoolName,
        Long profileImageLink,
        MemberStatus status
        // TODO: 활동 이력이나 각종 링크들 추가해야 함
) {
}
