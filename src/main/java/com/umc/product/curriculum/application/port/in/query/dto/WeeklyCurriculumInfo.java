package com.umc.product.curriculum.application.port.in.query.dto;

import com.umc.product.curriculum.domain.WeeklyCurriculum;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record WeeklyCurriculumInfo(
    Long id,

    Long curriculumId,

    boolean isExtra,

    String title,

    Long weekNo,

    Instant startsAt,

    Instant endsAt
) {

    public static WeeklyCurriculumInfo from(WeeklyCurriculum weeklyCurriculum) {
        return WeeklyCurriculumInfo.builder()
            .id(weeklyCurriculum.getId())
            .curriculumId(weeklyCurriculum.getCurriculum().getId())
            .isExtra(weeklyCurriculum.isExtra())
            .title(weeklyCurriculum.getTitle())
            .weekNo(weeklyCurriculum.getWeekNo())
            .startsAt(weeklyCurriculum.getStartsAt())
            .endsAt(weeklyCurriculum.getEndsAt())
            .build();
    }
}
