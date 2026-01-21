package com.umc.product.organization.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.time.Instant;
import java.util.List;

public record StudyGroupDetailInfo(
        Long groupId,
        String name,
        ChallengerPart part,
        List<SchoolInfo> schools,
        Instant createdAt,
        int memberCount,
        MemberInfo leader,
        List<MemberInfo> members
) {
    public record SchoolInfo(
            Long schoolId,
            String schoolName
    ) {
    }

    public record MemberInfo(
            Long challengerId,
            Long memberId,
            String name,
            String profileImageUrl
    ) {
    }
}
