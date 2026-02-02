package com.umc.product.schedule.adapter.in.web.dto.response;

import java.time.LocalTime;

// TODO : 주석 처리 부분 tags 로 변경
public record AvailableAttendanceResponse(
    Long scheduleId,
    String scheduleName,
//        String scheduleType,
    LocalTime startTime,
    LocalTime endTime,
    Long sheetId,
    Long recordId,
    String status,
    String statusDisplay
) {
}
