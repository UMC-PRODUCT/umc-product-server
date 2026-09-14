package com.umc.product.curriculum.application.port.in.query.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.domain.enums.MissionType;
import java.time.Instant;
import java.util.List;

public record CurriculumInfo(
    Long id,
    ChallengerPart part,
    String title,
    List<WorkbookInfo> workbooks
) {
    public record WorkbookInfo(
        Long id,
        Integer weekNo,
        String title,
        String description,
        String workbookUrl,
        Instant startDate,
        Instant endDate,
        MissionType missionType,
        Instant releasedAt,
        boolean isReleased
    ) {
    }
}
