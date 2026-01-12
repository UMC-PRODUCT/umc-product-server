package com.umc.product.schedule.adapter.in.web.dto.response;

import com.umc.product.schedule.application.port.in.dto.AttendanceSheetInfo;
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
    public static AttendanceSheetResponse from(AttendanceSheetInfo info) {
        return new AttendanceSheetResponse(
                info.id() != null ? info.id().id() : null,
                info.scheduleId(),
                info.window().getStartTime(),
                info.window().getEndTime(),
                info.window().getLateThresholdMinutes(),
                info.requiresApproval(),
                info.active()
        );
    }
}
