package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import java.time.Instant;
import java.util.List;

public record RecruitmentScheduleInfo(
        Long recruitmentId,
        List<ScheduleItem> schedules
) {
    public enum ScheduleKind {
        WINDOW,
        AT
    }

    public record ScheduleItem(
            RecruitmentScheduleType type,
            ScheduleKind kind,
            Instant startsAt,
            Instant endsAt
    ) {
    }
}
