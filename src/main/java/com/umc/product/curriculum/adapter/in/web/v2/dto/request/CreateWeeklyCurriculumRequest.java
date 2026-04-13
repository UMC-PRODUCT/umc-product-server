package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import java.time.Instant;

public record CreateWeeklyCurriculumRequest(
    Long curriculumId,
    Long weekNo,
    boolean isExtra,

    String title,
    Instant startsAt,
    Instant endsAt
) {
}
