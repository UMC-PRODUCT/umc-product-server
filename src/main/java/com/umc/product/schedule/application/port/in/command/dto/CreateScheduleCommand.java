package com.umc.product.schedule.application.port.in.command.dto;

import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.locationtech.jts.geom.Point;

/**
 * 일정 생성 Command
 */
public record CreateScheduleCommand(
    String name,
    Instant startsAt,
    Instant endsAt,
    boolean isAllDay,
    String locationName,
    Point location,
    String description,
    List<Long> participantMemberIds,
    Set<ScheduleTag> tags,
    Long authorMemberId
) {
    public CreateScheduleCommand {
        Objects.requireNonNull(name, "Schedule name must not be null");
    }

    public static CreateScheduleCommand of(
        String name,
        Instant startsAt,
        Instant endsAt,
        boolean isAllDay,
        String locationName,
        Point location,
        String description,
        List<Long> participantMemberIds,
        Set<ScheduleTag> tags,
        Long authorMemberId
    ) {
        return new CreateScheduleCommand(
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

    public Schedule toEntity(Long authorChallengerId) {
        return Schedule.builder()
            .name(name)
            .startsAt(startsAt)
            .endsAt(endsAt)
            .isAllDay(isAllDay)
            .locationName(locationName)
            .location(location)
            .description(description)
            .tags(tags)
            .authorChallengerId(authorChallengerId)
            .build();
    }

    public boolean hasParticipants() {
        return participantMemberIds != null && !participantMemberIds.isEmpty();
    }
}
