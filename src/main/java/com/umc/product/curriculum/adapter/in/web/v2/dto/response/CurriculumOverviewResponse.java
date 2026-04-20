package com.umc.product.curriculum.adapter.in.web.v2.dto.response;

import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record CurriculumOverviewResponse(
    Long curriculumId,
    String title,
    List<WeeklyCurriculumOverviewResponse> weeks
) {

    @Builder
    public record WeeklyCurriculumOverviewResponse(
        Long weeklyCurriculumId,
        Long weekNo,
        String title,
        boolean isExtra,
        Instant startsAt,
        Instant endsAt
    ) {

    }
}
