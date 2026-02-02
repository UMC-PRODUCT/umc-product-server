package com.umc.product.schedule.domain;

import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.vo.AttendanceWindow;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
public class AttendanceSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long scheduleId;

    @Embedded
    private AttendanceWindow window;

    @Column(nullable = false)
    private boolean requiresApproval;

    @Column(nullable = false)
    private boolean active;

    @Builder
    private AttendanceSheet(
        Long scheduleId,
        AttendanceWindow window,
        boolean requiresApproval
    ) {
        validateScheduleId(scheduleId);
        validateWindow(window);

        this.scheduleId = scheduleId;
        this.window = window;
        this.requiresApproval = requiresApproval;
        this.active = true;
    }

    /**
     * 체크 시간이 출석 가능 시간대(window) 안에 있는지 확인.
     */
    public boolean isWithinTimeWindow(LocalDateTime checkTime) {
        if (!active) {
            throw new IllegalStateException("비활성화된 출석부입니다");
        }
        return window.contains(checkTime);
    }

    /**
     * 체크 시간과 승인 정책(requiresApproval)을 기반으로 출석 상태를 결정한다. 실제 판정 로직은 AttendanceWindow.determineStatus()에 위임.
     */
    public AttendanceStatus determineStatusByTime(LocalDateTime checkTime) {
        if (!active) {
            throw new IllegalStateException("비활성화된 출석부입니다");
        }
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

    private void validateWindow(AttendanceWindow window) {
        if (window == null) {
            throw new IllegalArgumentException("출석 시간대는 필수입니다");
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
