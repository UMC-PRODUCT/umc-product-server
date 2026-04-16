package com.umc.product.schedule.adapter.in.web.v2.dto.request;

public record ExcuseScheduleAttendanceRequest(
    // === 위치 정보 ===
    Double latitude,
    Double longitude,
    String excuseReason
) {
}
