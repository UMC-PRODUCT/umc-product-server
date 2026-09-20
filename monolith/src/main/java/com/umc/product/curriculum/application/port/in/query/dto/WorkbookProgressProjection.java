package com.umc.product.curriculum.application.port.in.query.dto;

import java.time.Instant;

import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;

public record WorkbookProgressProjection(
    Long originalWorkbookId,
    Integer weekNo,
    String title,
    String description,
    MissionType missionType,
    Instant startDate,
    Instant endDate,
    Instant releasedAt,
    Long challengerWorkbookId,
    WorkbookStatus challengerWorkbookStatus
) {
}
