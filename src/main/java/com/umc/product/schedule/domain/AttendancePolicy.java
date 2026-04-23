package com.umc.product.schedule.domain;

import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 각 일정에 대한 출석 상태를 판단하는데 사용되는 기준.
 * <p>
 * (A) earlyCheckInMinutes (기본값 10분): 시작 시간 전에 몇 분까지 출석으로 인정할 것인지
 * <p>
 * (B) attendanceGraceMinutes (기본값 10분): 시작 시간 후에 몇 분까지 출석으로 인정할 것인지 (C)
 * <p>
 * (C) lateToleranceMinutes (기본값 10분): 출석 인정 시간이 종료된 후에 몇 분까지 지각으로 인정할 것인지
 * <p>
 * <p>
 * 출석으로 처리되는 시간: (시작시간 - A분) ~ (시작시간 + B분)
 * <p>
 * 지각으로 처리되는 시간: (시작시간 + B분) ~ (시작시간 + B분 + C분)
 * <p>
 * 결석은 그 후.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendancePolicy {

    private Long earlyCheckInMinutes;

    private Long attendanceGraceMinutes;

    private Long lateToleranceMinutes;

    private AttendancePolicy(
        Long earlyCheckInMinutes,
        Long attendanceGraceMinutes,
        Long lateToleranceMinutes
    ) {
        this.earlyCheckInMinutes = earlyCheckInMinutes;
        this.attendanceGraceMinutes = attendanceGraceMinutes;
        this.lateToleranceMinutes = lateToleranceMinutes;
    }

    /**
     * 매개변수로 제공된 checkInTime 및 scheduleStartTime을 기반으로 현재 상태를 판단합니다.
     * <p>
     * 최초 요청 시에만 사용하여야 하며, 상태 전이는 별도의 로직을 활용해야 합니다.
     * <p>
     * protected method로, 도메인 객체 내부에서만 사용할 수 있습니다.
     */
    protected AttendanceStatus getAttendanceStatusByPolicy(Instant checkInTime, Instant scheduleStartTime) {
        Instant earliestCheckIn = scheduleStartTime.minus(earlyCheckInMinutes, ChronoUnit.MINUTES);
        Instant latestCheckIn = scheduleStartTime.plus(attendanceGraceMinutes, ChronoUnit.MINUTES);
        Instant lateTolerance = latestCheckIn.plus(lateToleranceMinutes, ChronoUnit.MINUTES);

        if (checkInTime.isBefore(earliestCheckIn)) {
            throw new ScheduleDomainException(ScheduleErrorCode.CHECK_IN_TOO_EARLY);
        } else if (checkInTime.isBefore(latestCheckIn)) {
            return AttendanceStatus.PRESENT_PENDING;
        } else if (checkInTime.isBefore(lateTolerance)) {
            return AttendanceStatus.LATE_PENDING;
        } else {
            return AttendanceStatus.ABSENT;
        }
    }

    protected static AttendancePolicy create(
        Long earlyCheckInMinutes,
        Long attendanceGraceMinutes,
        Long lateToleranceMinutes) {

        return new AttendancePolicy(earlyCheckInMinutes, attendanceGraceMinutes, lateToleranceMinutes);
    }
}
