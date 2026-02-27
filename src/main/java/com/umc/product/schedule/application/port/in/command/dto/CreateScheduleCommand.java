package com.umc.product.schedule.application.port.in.command.dto;

import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.ScheduleConstants;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
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
        Instant adjustedStartsAt = startsAt;
        Instant adjustedEndsAt = endsAt;

        if (isAllDay) { // 하루종일 이면 KST 기준으로 시작/종료 시간 조정
            LocalDate startDate = startsAt.atZone(ScheduleConstants.KST).toLocalDate();
            LocalDate endDate = endsAt.atZone(ScheduleConstants.KST).toLocalDate();
            adjustedStartsAt = startDate.atStartOfDay(ScheduleConstants.KST).toInstant();
            adjustedEndsAt = endDate.atTime(LocalTime.of(23, 59, 59)).atZone(ScheduleConstants.KST).toInstant();
        }

        return new CreateScheduleCommand(
            name,
            adjustedStartsAt,
            adjustedEndsAt,
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
