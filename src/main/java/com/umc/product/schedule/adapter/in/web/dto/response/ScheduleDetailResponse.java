package com.umc.product.schedule.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleDetailInfo;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ScheduleDetailResponse(
    Long scheduleId,
    String name,
    String description,
    Set<String> tags,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime startsAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime endsAt,
    boolean isAllDay,
    String locationName,
    Double latitude,
    Double longitude,
    String status,
    long dDay,
    boolean requiresAttendanceApproval
) {

    public static ScheduleDetailResponse from(ScheduleDetailInfo info) {
        Set<String> tagNames = info.tags() != null
            ? info.tags().stream().map(Enum::name).collect(Collectors.toSet())
            : Set.of();

        return new ScheduleDetailResponse(
            info.scheduleId(),
            info.name(),
            info.description(),
            tagNames,
            info.startsAt(),
            info.endsAt(),
            info.isAllDay(),
            info.locationName(),
            info.latitude(),
            info.longitude(),
            info.status(),
            info.dDay(),
            info.requiresAttendanceApproval()
        );
    }
}
