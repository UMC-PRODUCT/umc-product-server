package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.RecruitmentScheduleType;
import java.time.Instant;
import java.util.List;

public record RecruitmentScheduleInfo(
        Long recruitmentId,
        List<ScheduleItem> schedules
) {
    public record ScheduleItem(
            RecruitmentScheduleType type,
            Instant startsAt,
            Instant endsAt,
            String note
    ) {
    }
}
