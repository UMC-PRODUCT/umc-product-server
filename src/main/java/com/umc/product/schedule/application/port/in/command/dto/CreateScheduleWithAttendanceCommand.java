package com.umc.product.schedule.application.port.in.command.dto;

import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.locationtech.jts.geom.Point;

/**
 * 일정 + 출석부 통합 생성 Command (Facade)
 */
public record CreateScheduleWithAttendanceCommand(
        // Schedule 정보
        String name,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        boolean isAllDay,
        String locationName,
        Point location,
        String description,
        List<Long> participantMemberIds,
        Set<ScheduleTag> tags,
        Long authorMemberId,
        // AttendanceSheet 정보
        AttendanceWindow attendanceWindow,
        boolean requiresApproval
) {
    public CreateScheduleWithAttendanceCommand {
        Objects.requireNonNull(name, "Schedule name must not be null");
        Objects.requireNonNull(attendanceWindow, "AttendanceWindow must not be null");
    }

    /**
     * Schedule 생성용 Command 추출
     */
    public CreateScheduleCommand toScheduleCommand() {
        return CreateScheduleCommand.of(
                name,
                startsAt,
                endsAt,
                isAllDay,
                locationName,
                location,
                description,
                participantMemberIds,
                tags,
                authorMemberId
        );
    }

    /**
     * AttendanceSheet 생성용 Command 추출
     */
    public CreateAttendanceSheetCommand toAttendanceSheetCommand(Long scheduleId) {
        return new CreateAttendanceSheetCommand(
                scheduleId,
                attendanceWindow,
                requiresApproval
        );
    }
}
