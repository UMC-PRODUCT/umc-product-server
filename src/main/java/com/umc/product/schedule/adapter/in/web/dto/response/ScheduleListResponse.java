package com.umc.product.schedule.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record ScheduleListResponse(
        Long scheduleId,
        String name,
        String type,
        String status,
        @JsonFormat(pattern = "yyyy.MM.dd (E)", locale = "ko_KR")
        LocalDateTime date,
        @JsonFormat(pattern = "HH:mm")
        LocalDateTime startTime,
        @JsonFormat(pattern = "HH:mm")
        LocalDateTime endTime,
        String locationName,
        Integer totalCount,
        Integer presentCount,
        Integer pendingCount,
        Double attendanceRate
) {
}
