package com.umc.product.schedule.adapter.in.web.dto.response;

import java.time.LocalDateTime;

public record AttendanceSheetResponse(
    Long id,
    Long scheduleId,
    LocalDateTime startTime,
    LocalDateTime endTime,
    int lateThresholdMinutes,
    boolean requiresApproval,
    boolean active
) {
}
