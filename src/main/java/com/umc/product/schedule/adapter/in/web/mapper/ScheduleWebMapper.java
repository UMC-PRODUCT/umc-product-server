package com.umc.product.schedule.adapter.in.web.mapper;

import com.umc.product.schedule.adapter.in.web.dto.response.ScheduleListResponse;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleWithStatsInfo;
import com.umc.product.schedule.domain.ScheduleConstants;
import java.util.List;
import org.springframework.stereotype.Component;

// TODO : 주석 처리 부분 tags 로 변경
@Component
public class ScheduleWebMapper {

    // Schedule
    public ScheduleListResponse toScheduleListResponse(ScheduleWithStatsInfo info) {
        return ScheduleListResponse.builder()
            .scheduleId(info.scheduleId())
            .name(info.name())
//                .type(info.type().name())
            .status(info.status())
            .date(info.endsAt().atZone(ScheduleConstants.KST).toLocalDateTime())
            .startTime(info.startsAt())
            .endTime(info.endsAt())
            .locationName(info.locationName())
            .totalCount(info.totalCount())
            .presentCount(info.presentCount())
            .pendingCount(info.pendingCount())
            .attendanceRate(info.attendanceRate())
            .build();
    }

    public List<ScheduleListResponse> toScheduleListResponses(List<ScheduleWithStatsInfo> infos) {
        return infos.stream().map(this::toScheduleListResponse).toList();
    }
}
