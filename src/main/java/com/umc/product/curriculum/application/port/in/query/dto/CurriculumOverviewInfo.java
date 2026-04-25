package com.umc.product.curriculum.application.port.in.query.dto;

import java.time.Instant;
import java.util.List;

public record CurriculumOverviewInfo(
    Long curriculumId,
    String title,
    List<WeeklyCurriculumOverviewInfo> weeks
) {

    public record WeeklyCurriculumOverviewInfo(
        Long weeklyCurriculumId,
        Long weekNo,
        String title,
        boolean isExtra,
        Instant startsAt,
        Instant endsAt
    ) {
    }
}
