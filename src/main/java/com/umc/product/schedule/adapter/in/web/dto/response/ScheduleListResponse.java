package com.umc.product.schedule.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

// TODO : 주석 처리 부분 tags 로 변경
public record ScheduleListResponse(
        Long scheduleId,
        String name,
//        String type,
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
