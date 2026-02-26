package com.umc.product.schedule.application.port.in.command.dto;

import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.locationtech.jts.geom.Point;

/**
 * 스터디 그룹 일정 생성 Command
 * <p>
 * participantMemberIds는 서비스에서 스터디 그룹 멤버로 자동 매핑됩니다.
 */
public record CreateStudyGroupScheduleCommand(
    String name,
    Instant startsAt,
    Instant endsAt,
    boolean isAllDay,
    String locationName,
    Point location,
    String description,
    Set<ScheduleTag> tags,
    Long studyGroupId,
    Long gisuId,
    boolean requiresApproval,
    Long authorMemberId
) {
    public CreateStudyGroupScheduleCommand {
        Objects.requireNonNull(name, "Schedule name must not be null");
        Objects.requireNonNull(startsAt, "Schedule startsAt must not be null");
        Objects.requireNonNull(endsAt, "Schedule endsAt must not be null");
        Objects.requireNonNull(studyGroupId, "studyGroupId must not be null");
        Objects.requireNonNull(gisuId, "gisuId must not be null");
    }

    /**
     * 스터디 그룹 멤버 목록을 받아 CreateScheduleWithAttendanceCommand로 변환
     */
    public CreateScheduleWithAttendanceCommand toScheduleWithAttendanceCommand(List<Long> memberIds) {
        return new CreateScheduleWithAttendanceCommand(
            name,
            startsAt,
            endsAt,
            isAllDay,
            locationName,
            location,
            description,
            memberIds,
            tags,
            authorMemberId,
            gisuId,
            requiresApproval
        );
    }
}
