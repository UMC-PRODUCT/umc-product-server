package com.umc.product.schedule.domain;

import com.umc.product.schedule.domain.vo.AttendanceWindow;
import com.umc.product.schedule.domain.vo.Location;
import com.umc.product.schedule.domain.vo.LocationRange;
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

//관리자가 출석부를 만드는 것
@Entity
@Table(name = "attendance_sheet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long scheduleId;  // 어떤 일정의 출석부인지 - 일정 코드를 보고 추가 수정 요망

    @Embedded
    private Location location;  // 출석 체크 위치

    @Embedded
    private LocationRange range;  // 출석 인정 반경 설정

    @Embedded
    private AttendanceWindow window;  // 출석 인정 시간

    @Column(nullable = false)
    private boolean requiresApproval;  // 승인 필요 여부 (지각/인정결석 승인)

    @Column(nullable = false)
    private boolean active;  // 활성화 여부

    @Builder
    private AttendanceSheet(
            Long scheduleId,
            Location location,
            LocationRange range,
            AttendanceWindow window,
            boolean requiresApproval
    ) {
        validateScheduleId(scheduleId);
        validateLocation(location);
        validateRange(range);
        validateWindow(window);

        this.scheduleId = scheduleId;
        this.location = location;
        this.range = range;
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

    //수정 파트
    public void update(Location location, LocationRange range, AttendanceWindow window) {
        validateLocation(location);
        validateRange(range);
        validateWindow(window);

        this.location = location;
        this.range = range;
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

    private void validateLocation(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("위치는 필수입니다");
        }
    }

    private void validateRange(LocationRange range) {
        if (range == null) {
            throw new IllegalArgumentException("위치 범위는 필수입니다");
        }
    }

    private void validateWindow(AttendanceWindow window) {
        if (window == null) {
            throw new IllegalArgumentException("출석 시간대는 필수입니다");
        }
    }

    public record AttendanceSheetId(long id) {
        public AttendanceSheetId {
            if (id <= 0) {
                throw new IllegalArgumentException("ID는 양수여야 합니다.");
            }
        }
    }

    public AttendanceSheetId getAttendanceSheetId() {
        if (this.id == null) {
            return null;
        }
        return new AttendanceSheetId(this.id);
    }
}
