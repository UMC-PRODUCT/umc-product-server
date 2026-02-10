package com.umc.product.schedule.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 출석부 엔티티. 하나의 Schedule(일정)에 1:1로 연결
 * <p>
 * 출석부는 출석 가능 시간대(AttendanceWindow)을 관리한다. 챌린저가 출석 체크를 하면 이 출석부의 시간대 설정을 기반으로 출석/지각/결석을 판정됨.
 * <p>
 * <p>- requiresApproval=true: 출석 체크 시 바로 확정되지 않고 PENDING 상태로 생성되서 관리자 승인을 거쳐야함
 * <p>- requiresApproval=false: 출석 체크 시 바로 PRESENT/LATE로 확정해줌
 * <p>- active: 삭제 대신 비활성화해소 기존 출석 기록을 보존
 */
@Entity
@Table(name = "attendance_sheet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceSheet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long scheduleId;

    @Column(nullable = false)
    private Long gisuId;

    @Embedded
    private AttendanceWindow window;

    @Column(nullable = false)
    private boolean requiresApproval;

    @Column(nullable = false)
    private boolean active;

    @Builder
    private AttendanceSheet(
        Long scheduleId,
        Long gisuId,
        AttendanceWindow window,
        boolean requiresApproval
    ) {
        validateScheduleId(scheduleId);
        validateGisuId(gisuId);
        validateWindow(window);

        this.scheduleId = scheduleId;
        this.gisuId = gisuId;
        this.window = window;
        this.requiresApproval = requiresApproval;
        this.active = true;
    }

    private static final int DEFAULT_LATE_THRESHOLD_MINUTES = 10;

    /**
     * 일정 기반 출석부 생성 (동시 생성용)
     * <p>
     * 출석 시간대(Window)는 일정의 시작/종료 시간을 기준으로 내부에서 생성됩니다.
     * 지각 기준 시간은 10분으로 고정됩니다.
     *
     * @param scheduleId            연결할 일정 ID
     * @param gisuId                기수 ID
     * @param scheduleStartsAt      일정 시작 시간 (출석 시간대 시작)
     * @param scheduleEndsAt        일정 종료 시간 (출석 시간대 종료)
     * @param requiresApproval      승인 필요 여부
     */
    public static AttendanceSheet createWithSchedule(
        Long scheduleId,
        Long gisuId,
        LocalDateTime scheduleStartsAt,
        LocalDateTime scheduleEndsAt,
        boolean requiresApproval
    ) {
        // 출석 인정 시간: 시작 10분 전 ~ 시작 후 10분 (총 20분)
        AttendanceWindow window = AttendanceWindow.of(
            scheduleStartsAt,   // 기준 시간
            10,                 // 10분 전부터
            10,                 // 10분 후까지
            0                   // lateThreshold 미사용 (윈도우 내는 모두 출석)
        );

        return AttendanceSheet.builder()
            .scheduleId(scheduleId)
            .gisuId(gisuId)
            .window(window)
            .requiresApproval(requiresApproval)
            .build();
    }

    /**
     * 체크 시간이 출석 가능 시간대(window) 안에 있는지 확인.
     * <p>
     * active 상태와 무관하게 시간대만 체크합니다.
     * active는 삭제 대신 비활성화 용도로만 사용됩니다.
     */
    public boolean isWithinTimeWindow(LocalDateTime checkTime) {
        return window.contains(checkTime);
    }

    /**
     * 체크 시간과 승인 정책(requiresApproval)을 기반으로 출석 상태를 결정한다. 실제 판정 로직은 AttendanceWindow.determineStatus()에 위임.
     * <p>
     * active 상태와 무관하게 시간대만 체크합니다.
     */
    public AttendanceStatus determineStatusByTime(LocalDateTime checkTime) {
        return window.determineStatus(checkTime, requiresApproval);
    }

    public void update(AttendanceWindow window) {
        validateWindow(window);
        this.window = window;
    }

    /**
     * 승인 모드 변경. true면 출석 체크 시 PENDING으로, false면 즉시 확정.
     */
    public void updateApprovalMode(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    /**
     * 출석부 비활성화 (기록 존재)
     */
    public void deactivate() {
        if (!active) {
            throw new IllegalStateException("이미 비활성화된 출석부입니다");
        }
        this.active = false;
    }

    /**
     * 비활성화된 출석부를 다시 활성화하는 기능,
     */
    public void activate() {
        if (active) {
            throw new IllegalStateException("이미 활성화된 출석부입니다");
        }
        this.active = true;
    }

    private void validateScheduleId(Long scheduleId) {
        if (scheduleId == null || scheduleId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 일정 ID입니다");
        }
    }

    private void validateGisuId(Long gisuId) {
        if (gisuId == null || gisuId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 기수 ID입니다");
        }
    }

    private void validateWindow(AttendanceWindow window) {
        if (window == null) {
            throw new IllegalArgumentException("출석 시간대는 필수입니다");
        }
    }

    /**
     * 일정 시간이 변경됨에 따라 출석 시간대도 같이 이동
     */
    public void shiftWindow(Duration diff) {
        if (diff != null && !diff.isZero()) {
            this.window = this.window.shift(diff);
        }
    }

    public AttendanceSheetId getAttendanceSheetId() {
        if (this.id == null) {
            return null;
        }
        return new AttendanceSheetId(this.id);
    }

    public record AttendanceSheetId(long id) {
        public AttendanceSheetId {
            if (id <= 0) {
                throw new IllegalArgumentException("ID는 양수여야 합니다.");
            }
        }
    }
}
