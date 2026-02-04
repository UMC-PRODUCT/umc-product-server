package com.umc.product.schedule.application.port.in.command.dto;

import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.locationtech.jts.geom.Point;

/**
 * 일정 + 출석부 통합 생성 Command (Facade)
 * <p>
 * 출석 시간대는 일정의 startsAt ~ endsAt을 기준으로 AttendanceSheet에서 자동 생성됩니다.
 * 지각 기준 시간은 10분으로 고정됩니다.
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
    Long gisuId,
    boolean requiresApproval
) {
    public CreateScheduleWithAttendanceCommand {
        Objects.requireNonNull(name, "Schedule name must not be null");
        Objects.requireNonNull(startsAt, "Schedule startsAt must not be null");
        Objects.requireNonNull(endsAt, "Schedule endsAt must not be null");
        Objects.requireNonNull(gisuId, "gisuId must not be null");
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
}
