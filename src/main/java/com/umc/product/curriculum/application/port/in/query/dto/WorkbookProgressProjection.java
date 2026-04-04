package com.umc.product.curriculum.application.port.in.query.dto;

import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import java.time.Instant;

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
