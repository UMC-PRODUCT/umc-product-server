package com.umc.product.organization.application.port.in.query.dto;

import java.util.List;

public record StudyGroupInfo(
        Long groupId,
        String name,
        int memberCount,
        LeaderInfo leader,
        List<MemberSummaryInfo> members
) {
}
