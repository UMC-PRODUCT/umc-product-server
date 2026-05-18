package com.umc.product.curriculum.adapter.in.web.v2.dto.response;

import com.umc.product.curriculum.application.port.in.query.dto.CurriculumOverviewInfo;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record CurriculumOverviewResponse(
    Long curriculumId,
    String title,
    List<WeeklyCurriculumOverviewResponse> weeks
) {

    public static CurriculumOverviewResponse from(CurriculumOverviewInfo info) {
        return CurriculumOverviewResponse.builder()
            .curriculumId(info.curriculumId())
            .title(info.title())
            .weeks(info.weeks().stream()
                .map(w -> WeeklyCurriculumOverviewResponse.builder()
                    .weeklyCurriculumId(w.weeklyCurriculumId())
                    .weekNo(w.weekNo())
                    .title(w.title())
                    .isExtra(w.isExtra())
                    .startsAt(w.startsAt())
                    .endsAt(w.endsAt())
                    .build())
                .toList())
            .build();
    }

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
