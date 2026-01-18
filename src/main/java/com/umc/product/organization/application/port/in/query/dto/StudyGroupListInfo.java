package com.umc.product.organization.application.port.in.query.dto;

import java.util.List;

public record StudyGroupListInfo(
        List<StudyGroupInfo> studyGroups,
        Long nextCursor,
        boolean hasNext
) {
}
