package com.umc.product.schedule.domain.vo;

import com.umc.product.schedule.domain.enums.AttendanceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 출석 시간대 상세
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceWindow {

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "late_threshold_minutes", nullable = false)
    private int lateThresholdMinutes;  // 몇 분까지 지각 인정

    private AttendanceWindow(LocalDateTime startTime, LocalDateTime endTime, int lateThresholdMinutes) {
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
        }
        this.startTime = startTime;
        this.endTime = endTime;
        this.lateThresholdMinutes = lateThresholdMinutes;
    }

    /**
     * 출석 시간대 생성 팩토리 메서드
     *
     * @param baseTime             기준 시간 (ex: 일정 시작 시간)
     * @param beforeMinutes        기준 시간 전 몇 분부터 출석 가능
     * @param afterMinutes         기준 시간 후 몇 분까지 출석 가능
     * @param lateThresholdMinutes 지각 인정 시간 (분)
     */
    public static AttendanceWindow of(
            LocalDateTime baseTime,
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

        LocalDateTime start = baseTime.minusMinutes(beforeMinutes);
        LocalDateTime end = baseTime.plusMinutes(afterMinutes);

        return new AttendanceWindow(start, end, lateThresholdMinutes);
    }

    /**
     * 기본 출석 시간대 생성 (30분 전 ~ 30분 후, 10분 지각 인정)
     */
    public static AttendanceWindow ofDefault(LocalDateTime baseTime) {
        return of(baseTime, 30, 30, 10);
    }

    /**
     * 시작/종료 시간을 직접 지정하여 출석 시간대 생성
     *
     * @param startTime            출석 시작 시간
     * @param endTime              출석 종료 시간
     * @param lateThresholdMinutes 지각 인정 시간 (분)
     */
    public static AttendanceWindow from(
            LocalDateTime startTime,
            LocalDateTime endTime,
            int lateThresholdMinutes
    ) {
        return new AttendanceWindow(startTime, endTime, lateThresholdMinutes);
    }

    /**
     * 주어진 시간이 출석 시간대 내에 있는지 확인
     */
    public boolean contains(LocalDateTime checkTime) {
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
    public AttendanceStatus determineStatus(LocalDateTime checkTime, boolean requiresApproval) {
        if (checkTime == null) {
            throw new IllegalArgumentException("체크 시간은 필수입니다");
        }

        // 시간대 밖
        if (!contains(checkTime)) {
            return AttendanceStatus.ABSENT;
        }

        // 출석 인정 시간 계산
        LocalDateTime lateThreshold = startTime.plusMinutes(lateThresholdMinutes);

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
}
