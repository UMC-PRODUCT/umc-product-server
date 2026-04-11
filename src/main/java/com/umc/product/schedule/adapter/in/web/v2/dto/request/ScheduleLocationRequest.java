package com.umc.product.schedule.adapter.in.web.v2.dto.request;

public record ScheduleLocationRequest(
    Double latitude,
    Double longitude,
    String locationName
) {
}
