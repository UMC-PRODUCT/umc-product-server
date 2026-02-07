package com.umc.product.schedule.domain.vo;

import com.umc.product.schedule.domain.enums.AttendanceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Duration;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 출석 가능 시간대를 표현하는 Value Object (AttendanceSheet에 @Embedded로 포함됨)
 * <p>
 * 출석 판정의 핵심 로직 클래스 startTime ~ endTime 범위 내에서 체크인한 시각에 따라 출석/지각을 구분
 * <p>
 * 판정 기준 (determineStatus) startTime, 출석, startTime + lateThresholdMinutes, 지각, endTime 이중 startTime +
 * lateThresholdMinutes 파트가 출석인정 endTime 이후 체크인은 결석
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceWindow {

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "late_threshold_minutes", nullable = false)
    private int lateThresholdMinutes;  // 몇 분까지 지각 인정

    private AttendanceWindow(Instant startTime, Instant endTime, int lateThresholdMinutes) {
        if (startTime == null) {
            throw new IllegalArgumentException("시작 시간은 필수입니다");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("종료 시간은 필수입니다");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 이전이어야 합니다");
        }
        if (lateThresholdMinutes < 0) {
            throw new IllegalArgumentException("지각 인정 시간은 0분 이상이어야 합니다");
        }
        if (lateThresholdMinutes > 120) {
            throw new IllegalArgumentException("지각 인정 시간은 120분을 초과할 수 없습니다");
            //스터디를 보통 2시간 정도 하는 것같아서 세운 기준
        }
        this.startTime = startTime;
        this.endTime = endTime;
        this.lateThresholdMinutes = lateThresholdMinutes;
    }

    /**
     * 출석 시간대 생성 파트
     *
     * @param baseTime             기준 시간
     * @param beforeMinutes        기준 시간 전 몇 분부터 출석 가능
     * @param afterMinutes         기준 시간 후 몇 분까지 출석 가능
     * @param lateThresholdMinutes 지각 인정 시간
     */
    public static AttendanceWindow of(
        Instant baseTime,
        int beforeMinutes,
        int afterMinutes,
        int lateThresholdMinutes
    ) {
        if (baseTime == null) {
            throw new IllegalArgumentException("기준 시간은 필수입니다");
        }
        if (beforeMinutes < 0) {
            throw new IllegalArgumentException("이전 시간은 0분 이상이어야 합니다");
        }
        if (afterMinutes < 0) {
            throw new IllegalArgumentException("이후 시간은 0분 이상이어야 합니다");
        }
;
        Instant start = baseTime.minus(Duration.ofMinutes(beforeMinutes));
        Instant end = baseTime.plus(Duration.ofMinutes(afterMinutes));

        return new AttendanceWindow(start, end, lateThresholdMinutes);
    }

    /**
     * 기본 출석 시간대 생성 - 테스트용 신경 쓸 필요 Xx
     */
    public static AttendanceWindow ofDefault(Instant baseTime) {
        return of(baseTime, 30, 30, 10);
    }

    /**
     * 시작/종료 시간을 직접 지정하여 출석 시간대 생성
     *
     * @param startTime            출석 시작 시간
     * @param endTime              출석 종료 시간
     * @param lateThresholdMinutes 지각 인정 시간
     */
    public static AttendanceWindow from(
        Instant startTime,
        Instant endTime,
        int lateThresholdMinutes
    ) {
        return new AttendanceWindow(startTime, endTime, lateThresholdMinutes);
    }

    /**
     * 주어진 시간이 출석 시간대 내에 있는지 확인
     */
    public boolean contains(Instant checkTime) {
        if (checkTime == null) {
            return false;
        }
        return !checkTime.isBefore(startTime) && !checkTime.isAfter(endTime);
    }

    /**
     * 체크 시간에 따른 출석 상태 결정 위치 검증은 이미 완료된 상태에서 호출됨
     *
     * @param checkTime        체크한 시간
     * @param requiresApproval 승인 필요 여부
     * @return 결정된 출석 상태
     */
    public AttendanceStatus determineStatus(Instant checkTime, boolean requiresApproval) {
        if (checkTime == null) {
            throw new IllegalArgumentException("체크 시간은 필수입니다");
        }

        // 시간대 밖
        if (!contains(checkTime)) {
            return AttendanceStatus.ABSENT;
        }

        // 출석 인정 시간 계산
        Instant lateThreshold = startTime.plus(Duration.ofMinutes(lateThresholdMinutes));

        // 정시 출석
        if (!checkTime.isAfter(lateThreshold)) {
            return requiresApproval
                ? AttendanceStatus.PRESENT_PENDING
                : AttendanceStatus.PRESENT;
        }

        // 지각
        return requiresApproval
            ? AttendanceStatus.LATE_PENDING
            : AttendanceStatus.LATE;
    }

    /**
     * 시간 차이만큼 출석 시간대를 이동시킴 (일정 수정 - 시간 변경 시 사용)
     *
     * @param duration 이동할 시간 간격 (양수면 미래로, 음수면 과거로)
     */
    public AttendanceWindow shift(Duration duration) {
        if (duration == null || duration.isZero()) {
            return this;
        }
        return new AttendanceWindow(
            this.startTime.plus(duration),
            this.endTime.plus(duration),
            this.lateThresholdMinutes
        );
    }
}
