package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import com.umc.product.curriculum.domain.enums.MissionType;

public record CreateOriginalWorkbookMissionRequest(
    Long originalWorkbookId,
    String title,
    String description,
    MissionType missionType,
    boolean isNecessary
) {
}
