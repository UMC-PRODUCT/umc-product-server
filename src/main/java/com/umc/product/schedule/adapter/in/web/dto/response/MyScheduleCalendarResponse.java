package com.umc.product.schedule.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.umc.product.schedule.application.port.in.query.dto.MyScheduleCalendarInfo;
import java.time.LocalDateTime;

public record MyScheduleCalendarResponse(
        Long scheduleId,
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime startsAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime endsAt,
        String status,
        long dDay
) {

    public static MyScheduleCalendarResponse from(MyScheduleCalendarInfo info) {
        return new MyScheduleCalendarResponse(
                info.scheduleId(),
                info.name(),
                info.startsAt(),
                info.endsAt(),
                info.status(),
                info.dDay()
        );
    }
}
