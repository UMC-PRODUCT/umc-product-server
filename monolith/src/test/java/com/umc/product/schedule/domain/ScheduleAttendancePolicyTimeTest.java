package com.umc.product.schedule.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;

@DisplayName("Schedule 출석 정책 시각 검증")
class ScheduleAttendancePolicyTimeTest {

    // 14:00 시작, 16:00 종료
    private static final Instant STARTS_AT = Instant.parse("2026-03-02T14:00:00Z");
    private static final Instant ENDS_AT = Instant.parse("2026-03-02T16:00:00Z");

    private static Instant fromStart(long amount, ChronoUnit unit) {
        return STARTS_AT.plus(amount, unit);
    }

    private static AttendancePolicy create(Instant checkInStartAt, Instant onTimeEndAt, Instant lateEndAt) {
        return Schedule.createAttendancePolicy(checkInStartAt, onTimeEndAt, lateEndAt, STARTS_AT, ENDS_AT);
    }

    @Nested
    @DisplayName("유예 구간")
    class Grace {

        @Test
        @DisplayName("유예 0분을 허용한다 (시작 정각까지만 출석으로 인정하는 정책)")
        void allowsZeroGrace() {
            AttendancePolicy policy = create(
                fromStart(-10, ChronoUnit.MINUTES),
                STARTS_AT,
                fromStart(11, ChronoUnit.MINUTES)
            );

            assertThat(policy.getEarlyCheckInMinutes()).isEqualTo(10L);
            assertThat(policy.getAttendanceGraceMinutes()).isZero();
            assertThat(policy.getLateToleranceMinutes()).isEqualTo(11L);
        }

        @Test
        @DisplayName("유예가 양수인 기존 정책도 그대로 동작한다")
        void allowsPositiveGrace() {
            AttendancePolicy policy = create(
                fromStart(-10, ChronoUnit.MINUTES),
                fromStart(10, ChronoUnit.MINUTES),
                fromStart(20, ChronoUnit.MINUTES)
            );

            assertThat(policy.getAttendanceGraceMinutes()).isEqualTo(10L);
        }

        @Test
        @DisplayName("유예 구간이 역전되면 거부한다")
        void rejectsNegativeGrace() {
            assertThatThrownBy(() -> create(
                fromStart(-10, ChronoUnit.MINUTES),
                fromStart(-1, ChronoUnit.MINUTES),
                fromStart(11, ChronoUnit.MINUTES)
            ))
                .isInstanceOf(ScheduleDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ScheduleErrorCode.INVALID_TIME_RANGE);
        }
    }

    @Nested
    @DisplayName("조기 출석과 지각 구간은 1분 이상이어야 한다")
    class MinimumOneMinute {

        @Test
        @DisplayName("조기 출석 0분을 거부한다")
        void rejectsZeroEarlyCheckIn() {
            assertThatThrownBy(() -> create(
                STARTS_AT,
                STARTS_AT,
                fromStart(11, ChronoUnit.MINUTES)
            ))
                .isInstanceOf(ScheduleDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ScheduleErrorCode.INVALID_TIME_RANGE);
        }

        @Test
        @DisplayName("지각 0분을 거부한다")
        void rejectsZeroLateTolerance() {
            assertThatThrownBy(() -> create(
                fromStart(-10, ChronoUnit.MINUTES),
                STARTS_AT,
                STARTS_AT
            ))
                .isInstanceOf(ScheduleDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ScheduleErrorCode.INVALID_TIME_RANGE);
        }

        @Test
        @DisplayName("지각 마감이 일정 종료와 같으면 거부한다")
        void rejectsLateEndEqualToScheduleEnd() {
            assertThatThrownBy(() -> create(
                fromStart(-10, ChronoUnit.MINUTES),
                STARTS_AT,
                ENDS_AT
            ))
                .isInstanceOf(ScheduleDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ScheduleErrorCode.INVALID_TIME_RANGE);
        }
    }

    @Nested
    @DisplayName("분 단위 정밀도")
    class Precision {

        @Test
        @DisplayName("유예 구간에 초가 섞이면 거부한다 (절삭되어 0분으로 저장되는 것을 막는다)")
        void rejectsSecondsInGrace() {
            assertThatThrownBy(() -> create(
                fromStart(-10, ChronoUnit.MINUTES),
                fromStart(30, ChronoUnit.SECONDS),
                fromStart(11, ChronoUnit.MINUTES)
            ))
                .isInstanceOf(ScheduleDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ScheduleErrorCode.INVALID_TIME_RANGE);
        }

        @Test
        @DisplayName("조기 출석 구간에 초가 섞이면 거부한다")
        void rejectsSecondsInEarlyCheckIn() {
            assertThatThrownBy(() -> create(
                fromStart(-90, ChronoUnit.SECONDS),
                STARTS_AT,
                fromStart(11, ChronoUnit.MINUTES)
            ))
                .isInstanceOf(ScheduleDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ScheduleErrorCode.INVALID_TIME_RANGE);
        }

        @Test
        @DisplayName("지각 구간에 초가 섞이면 거부한다")
        void rejectsSecondsInLateTolerance() {
            assertThatThrownBy(() -> create(
                fromStart(-10, ChronoUnit.MINUTES),
                STARTS_AT,
                fromStart(630, ChronoUnit.SECONDS)
            ))
                .isInstanceOf(ScheduleDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ScheduleErrorCode.INVALID_TIME_RANGE);
        }

        @Test
        @DisplayName("모든 시각에 같은 초가 있어도 간격이 정수 분이면 허용한다")
        void allowsUniformSecondsWhenIntervalsAreWholeMinutes() {
            Instant shifted = STARTS_AT.plusSeconds(30);

            AttendancePolicy policy = Schedule.createAttendancePolicy(
                shifted.minus(10, ChronoUnit.MINUTES),
                shifted,
                shifted.plus(11, ChronoUnit.MINUTES),
                shifted,
                ENDS_AT
            );

            assertThat(policy.getEarlyCheckInMinutes()).isEqualTo(10L);
            assertThat(policy.getAttendanceGraceMinutes()).isZero();
            assertThat(policy.getLateToleranceMinutes()).isEqualTo(11L);
        }

        @Test
        @DisplayName("나노초가 섞이면 거부한다")
        void rejectsNanosInGrace() {
            assertThatThrownBy(() -> create(
                fromStart(-10, ChronoUnit.MINUTES),
                STARTS_AT.plusNanos(1),
                fromStart(11, ChronoUnit.MINUTES)
            ))
                .isInstanceOf(ScheduleDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ScheduleErrorCode.INVALID_TIME_RANGE);
        }
    }

    @Nested
    @DisplayName("유예 0분 정책의 실제 출석 판정 경계 (조기 10분, 지각 11분)")
    class ZeroGraceBoundary {

        private AttendancePolicy studyPolicy() {
            return create(
                fromStart(-10, ChronoUnit.MINUTES),
                STARTS_AT,
                fromStart(11, ChronoUnit.MINUTES)
            );
        }

        @Test
        @DisplayName("조기 출석 경계 직전은 CHECK_IN_TOO_EARLY")
        void beforeEarlyCheckInWindow() {
            assertThatThrownBy(() -> studyPolicy()
                .getAttendanceStatusByPolicy(fromStart(-10, ChronoUnit.MINUTES).minusMillis(1), STARTS_AT))
                .isInstanceOf(ScheduleDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ScheduleErrorCode.CHECK_IN_TOO_EARLY);
        }

        @Test
        @DisplayName("조기 출석 경계 정각은 출석")
        void atEarlyCheckInWindow() {
            assertThat(studyPolicy()
                .getAttendanceStatusByPolicy(fromStart(-10, ChronoUnit.MINUTES), STARTS_AT))
                .isEqualTo(AttendanceStatus.PRESENT_PENDING);
        }

        @Test
        @DisplayName("시작 정각 직전은 출석")
        void justBeforeStartIsPresent() {
            assertThat(studyPolicy().getAttendanceStatusByPolicy(STARTS_AT.minusMillis(1), STARTS_AT))
                .isEqualTo(AttendanceStatus.PRESENT_PENDING);
        }

        @Test
        @DisplayName("시작 정각은 지각 (경계값을 출석에 포함하지 않는다)")
        void atStartIsLate() {
            assertThat(studyPolicy().getAttendanceStatusByPolicy(STARTS_AT, STARTS_AT))
                .isEqualTo(AttendanceStatus.LATE_PENDING);
        }

        @Test
        @DisplayName("지각 마감 직전은 지각")
        void justBeforeLateEndIsLate() {
            assertThat(studyPolicy()
                .getAttendanceStatusByPolicy(fromStart(11, ChronoUnit.MINUTES).minusMillis(1), STARTS_AT))
                .isEqualTo(AttendanceStatus.LATE_PENDING);
        }

        @Test
        @DisplayName("지각 마감 정각은 결석 (경계값을 지각에 포함하지 않는다)")
        void atLateEndIsAbsent() {
            assertThat(studyPolicy()
                .getAttendanceStatusByPolicy(fromStart(11, ChronoUnit.MINUTES), STARTS_AT))
                .isEqualTo(AttendanceStatus.ABSENT);
        }
    }
}
