package com.umc.product.schedule.adapter.in.web.mapper;

import com.umc.product.schedule.adapter.in.web.dto.response.AttendanceSheetResponse;
import com.umc.product.schedule.application.port.in.query.dto.AttendanceSheetInfo;
import org.springframework.stereotype.Component;

@Component
public class AttendanceSheetWebMapper {

    public AttendanceSheetResponse toAttendanceSheetResponse(AttendanceSheetInfo info) {
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
