package com.umc.product.organization.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.time.LocalDateTime;
import java.util.List;

public record StudyGroupDetailInfo(
        Long groupId,
        String name,
        ChallengerPart part,
        Long schoolId,
        String schoolName,
        LocalDateTime createdAt,
        int memberCount,
        MemberInfo leader,
        List<MemberInfo> members
) {
    public record MemberInfo(
            Long challengerId,
            Long memberId,
            String name,
            String profileImageUrl
    ) {
    }
}
