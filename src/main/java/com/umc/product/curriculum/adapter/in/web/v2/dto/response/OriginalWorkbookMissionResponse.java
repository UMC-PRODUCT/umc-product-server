package com.umc.product.curriculum.adapter.in.web.v2.dto.response;

import com.umc.product.curriculum.domain.enums.MissionType;

public record OriginalWorkbookMissionResponse(
    Long originalWorkbookId,
    Long originalWorkbookMissionId,
    String title,
    String description,
    MissionType missionType,
    boolean isNecessary
) {
}
