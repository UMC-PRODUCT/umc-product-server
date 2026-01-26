package com.umc.product.schedule.adapter.in.web.mapper;

import com.umc.product.schedule.adapter.in.web.dto.response.ScheduleListResponse;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleWithStatsInfo;
import java.util.List;
import org.springframework.stereotype.Component;

// TODO : 주석 처리 부분 tags 로 변경
@Component
public class ScheduleWebMapper {

    // Schedule
    public ScheduleListResponse toScheduleListResponse(ScheduleWithStatsInfo info) {
        return new ScheduleListResponse(
                info.scheduleId(),
                info.name(),
//                info.type().name(),
                info.status(),
                info.startsAt(),
                info.startsAt(),
                info.endsAt(),
                info.locationName(),
                info.totalCount(),
                info.presentCount(),
                info.pendingCount(),
                info.attendanceRate()
        );
    }

    public List<ScheduleListResponse> toScheduleListResponses(List<ScheduleWithStatsInfo> infos) {
        return infos.stream().map(this::toScheduleListResponse).toList();
    }
}
