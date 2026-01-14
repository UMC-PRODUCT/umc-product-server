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

    //시간 검증
    public boolean isWithinTimeWindow(LocalDateTime checkTime) {
        if (!active) {
            throw new IllegalStateException("비활성화된 출석부입니다");
        }
        return window.contains(checkTime);
    }

    //시간 기반 상태 결정
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

    //승인 모드 변경
    public void updateApprovalMode(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    //출석 기록 관리 쌓이는거때매 우선 삭제가 아닌 비활성화 느낌으로 만들었습니다
    public void deactivate() {
        if (!active) {
            throw new IllegalStateException("이미 비활성화된 출석부입니다");
        }
        this.active = false;
    }

    //활성화
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
