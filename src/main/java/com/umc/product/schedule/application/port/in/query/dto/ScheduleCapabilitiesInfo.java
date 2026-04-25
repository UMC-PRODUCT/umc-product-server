package com.umc.product.schedule.application.port.in.query.dto;

import lombok.AccessLevel;
import lombok.Builder;

/**
 * 일정 생성 관련 사용자 권한 정보
 */
@Builder(access = AccessLevel.PRIVATE)
public record ScheduleCapabilitiesInfo(
    boolean canCreateSchedule,
    boolean canCreateAttendanceRequiredSchedule,
    int maxParticipantCount
) {

    // 일반 챌린저
    private static final int DEFAULT_MAX_PARTICIPANTS = 50;

    // 교내
    private static final int SCHOOL_ADMIN_MAX_PARTICIPANTS = 100;
    private static final int SCHOOL_CORE_MAX_PARTICIPANTS = 100;

    // 지부장
    private static final int CHAPTER_PRESIDENT_MAX_PARTICIPANTS = 300;

    // 중앙
    private static final int CENTRAL_MEMBER_MAX_PARTICIPANTS = 300;
    private static final int CENTRAL_CORE_MAX_PARTICIPANTS = 2000;

    public static ScheduleCapabilitiesInfo notAllowed() {
        return ScheduleCapabilitiesInfo.builder()
            .canCreateSchedule(false)
            .canCreateAttendanceRequiredSchedule(false)
            .maxParticipantCount(0)
            .build();
    }

    public static ScheduleCapabilitiesInfo forChallenger() {
        return ScheduleCapabilitiesInfo.builder()
            .canCreateSchedule(true)
            .canCreateAttendanceRequiredSchedule(false)
            .maxParticipantCount(DEFAULT_MAX_PARTICIPANTS)
            .build();
    }

    public static ScheduleCapabilitiesInfo forSchoolAdmin() {
        return ScheduleCapabilitiesInfo.builder()
            .canCreateSchedule(true)
            .canCreateAttendanceRequiredSchedule(true)
            .maxParticipantCount(SCHOOL_ADMIN_MAX_PARTICIPANTS)
            .build();
    }

    public static ScheduleCapabilitiesInfo forCentralMember() {
        return ScheduleCapabilitiesInfo.builder()
            .canCreateSchedule(true)
            .canCreateAttendanceRequiredSchedule(true)
            .maxParticipantCount(CENTRAL_MEMBER_MAX_PARTICIPANTS)
            .build();
    }

    public static ScheduleCapabilitiesInfo forSchoolCore() {
        return ScheduleCapabilitiesInfo.builder()
            .canCreateSchedule(true)
            .canCreateAttendanceRequiredSchedule(true)
            .maxParticipantCount(SCHOOL_CORE_MAX_PARTICIPANTS)
            .build();
    }

    public static ScheduleCapabilitiesInfo forChapterPresident() {
        return ScheduleCapabilitiesInfo.builder()
            .canCreateSchedule(true)
            .canCreateAttendanceRequiredSchedule(true)
            .maxParticipantCount(CHAPTER_PRESIDENT_MAX_PARTICIPANTS)
            .build();
    }

    public static ScheduleCapabilitiesInfo forCentralCore() {
        return ScheduleCapabilitiesInfo.builder()
            .canCreateSchedule(true)
            .canCreateAttendanceRequiredSchedule(true)
            .maxParticipantCount(CENTRAL_CORE_MAX_PARTICIPANTS)
            .build();
    }
}
