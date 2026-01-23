package com.umc.product.schedule.domain;

import com.umc.product.schedule.domain.enums.AttendanceStatus;
import jakarta.persistence.Column;
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

@Entity
@Table(name = "attendance_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long attendanceSheetId;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    private LocalDateTime checkedAt;

    @Column(length = 500)
    private String memo;

    @Builder
    private AttendanceRecord(
            Long attendanceSheetId,
            Long memberId,
            AttendanceStatus status,
            LocalDateTime checkedAt,
            String memo
    ) {
        validateAttendanceSheetId(attendanceSheetId);
        validateMemberId(memberId);
        validateStatus(status);

        this.attendanceSheetId = attendanceSheetId;
        this.memberId = memberId;
        this.status = status;
        this.checkedAt = checkedAt;
        this.memo = memo;
    }

    // Domain Logic: 출석 체크
    public void checkIn(AttendanceStatus newStatus, LocalDateTime checkedAt) {
        if (this.checkedAt != null) {
            throw new IllegalStateException("이미 출석 체크가 완료되었습니다");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("출석 상태는 필수입니다");
        }
        if (checkedAt == null) {
            throw new IllegalArgumentException("체크 시간은 필수입니다");
        }

        this.status = newStatus;
        this.checkedAt = checkedAt;
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

    public void updateCheckInfo(LocalDateTime checkedAt) {
        if (this.checkedAt == null) {
            throw new IllegalStateException("출석 체크가 되지 않은 기록입니다");
        }
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

    private void validateMemberId(Long memberId) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 멤버 ID입니다");
        }
    }

    private void validateStatus(AttendanceStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("출석 상태는 필수입니다");
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

    public String getStatusDisplay() {
        return switch (status) {
            case PRESENT -> "출석";
            case LATE -> "지각";
            case ABSENT -> "결석";
            case EXCUSED -> "인정";
            case PENDING, PRESENT_PENDING, LATE_PENDING, EXCUSED_PENDING -> "대기";
        };
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
}
