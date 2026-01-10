package com.umc.product.schedule.domain;

import com.umc.product.schedule.domain.vo.Location;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//여기가 챌린적가 요청하는 파트
@Entity
@Table(name = "attendance_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long attendanceSheetId;  // 어떤 출석부의 기록인지

    @Column(nullable = false)
    private Long challengerId;  // 누구의 기록인지

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @Embedded
    private Location checkedLocation;  // 실제 체크한 위치

    private LocalDateTime checkedAt;  // 실제 체크한 시간

    @Column(length = 500)
    private String memo;  // 사유서 등

    @Builder
    private AttendanceRecord(
            Long attendanceSheetId,
            Long challengerId,
            AttendanceStatus status,
            Location checkedLocation,
            LocalDateTime checkedAt,
            String memo
    ) {
        validateAttendanceSheetId(attendanceSheetId);
        validateChallengerId(challengerId);
        validateStatus(status);

        this.attendanceSheetId = attendanceSheetId;
        this.challengerId = challengerId;
        this.status = status;
        this.checkedLocation = checkedLocation;
        this.checkedAt = checkedAt;
        this.memo = memo;
    }

    // Domain Logic: 승인
    public void approve() {
        validatePendingStatus();

        this.status = switch (status) {
            case PRESENT_PENDING -> AttendanceStatus.PRESENT;
            case LATE_PENDING -> AttendanceStatus.LATE;
            case EXCUSED_PENDING -> AttendanceStatus.EXCUSED;
            default -> throw new IllegalStateException("승인 가능한 상태가 아닙니다: " + status);
        };
    }

    // Domain Logic: 반려
    public void reject() {
        validatePendingStatus();
        this.status = AttendanceStatus.ABSENT;
    }

    // Domain Logic: 인정결석 신청
    public void requestExcuse(String memo) {
        if (status != AttendanceStatus.ABSENT) {
            throw new IllegalStateException("결석 상태만 인정결석을 신청할 수 있습니다");
        }
        if (memo == null || memo.isBlank()) {
            throw new IllegalArgumentException("사유는 필수입니다");
        }

        this.status = AttendanceStatus.EXCUSED_PENDING;
        this.memo = memo;
    }

    // Domain Logic: 상태 변경
    public void updateStatus(AttendanceStatus newStatus) {
        validateStatus(newStatus);

        // PENDING 상태는 approve/reject 메서드로만 변경 가능
        if (newStatus.name().endsWith("_PENDING")) {
            throw new IllegalArgumentException("PENDING 상태는 직접 변경할 수 없습니다");
        }

        this.status = newStatus;
    }

    // Domain Logic: 출석 체크 정보 수정
    public void updateCheckInfo(Location location, LocalDateTime checkedAt) {
        if (this.checkedLocation == null) {
            throw new IllegalStateException("출석 체크가 되지 않은 기록입니다");
        }
        this.checkedLocation = location;
        this.checkedAt = checkedAt;
    }

    public boolean isPending() {
        return status.name().endsWith("_PENDING");
    }

    public boolean isChecked() {
        return checkedAt != null;
    }

    private void validatePendingStatus() {
        if (!isPending()) {
            throw new IllegalStateException("승인 대기 상태가 아닙니다: " + status);
        }
    }

    private void validateAttendanceSheetId(Long attendanceSheetId) {
        if (attendanceSheetId == null || attendanceSheetId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 출석부 ID입니다");
        }
    }

    private void validateChallengerId(Long challengerId) {
        if (challengerId == null || challengerId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 챌린저 ID입니다");
        }
    }

    private void validateStatus(AttendanceStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("출석 상태는 필수입니다");
        }
    }

    /**
     * 출석 기록 ID (타입 안전성을 위한 래퍼)
     */
    public record AttendanceRecordId(long id) {
        public AttendanceRecordId {
            if (id <= 0) {
                throw new IllegalArgumentException("ID는 양수여야 합니다.");
            }
        }
    }

    /**
     * 타입 안전한 ID 반환
     */
    public AttendanceRecordId getAttendanceRecordId() {
        if (this.id == null) {
            return null;
        }
        return new AttendanceRecordId(this.id);
    }
}
