package com.umc.product.schedule.domain;

import com.umc.product.common.BaseEntity;
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

/**
 * 개별 챌린저의 출석 기록 파트
 * <p>
 * 하나의 AttendanceSheet(출석부)에 소속된 챌린저별 출석 상태를 관리한다. 상태 변경은 반드시 아래 도메인 메서드를 통해서만 이루어지며, 외부에서 직접 상태를 수정할 수 없음.
 * <p>
 * 상태 규칙 PENDING ──checkIn()──→ PRESENT / LATE / PRESENT_PENDING / LATE_PENDING PENDING ──approve()──→ PRESENT / LATE/
 * EXCUSED  (승인자 ID 기록 남도록) PENDING ──reject()───→ ABSENT──requestExcuse()──→ EXCUSED_PENDING        (사유 memo 필수)
 */
@Entity
@Table(name = "attendance_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceRecord extends BaseEntity {

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

    private Long confirmedBy;

    private LocalDateTime confirmedAt;

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

    /**
     * 출석 체크 처리. 한 번만 호출 가능하며, 이미 체크된 기록에 재호출하면 예외 발생. newStatus는 AttendanceSheet.determineStatusByTime()이 시간 기반으로 결정
     */
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

    /**
     * 관리자가 PENDING 상태의 출석을 승인한다. PRESENT_PENDING → PRESENT, LATE_PENDING → LATE, EXCUSED_PENDING → EXCUSED로 전이. 승인자 ID와
     * 승인 시각이 함께 기록된다.
     */
    public void approve(Long confirmerId) {
        validatePendingStatus();
        validateConfirmerId(confirmerId);

        this.status = switch (status) {
            case PRESENT_PENDING -> AttendanceStatus.PRESENT;
            case LATE_PENDING -> AttendanceStatus.LATE;
            case EXCUSED_PENDING -> AttendanceStatus.EXCUSED;
            default -> throw new IllegalStateException("승인 가능한 상태가 아닙니다: " + status);
        };
        this.confirmedBy = confirmerId;
        this.confirmedAt = LocalDateTime.now();
    }

    /**
     * 관리자가 PENDING 상태의 출석을 거절함. 상태는 무조건 ABSENT로 바뀜
     */
    public void reject(Long confirmerId) {
        validatePendingStatus();
        validateConfirmerId(confirmerId);

        this.status = AttendanceStatus.ABSENT;
        this.confirmedBy = confirmerId;
        this.confirmedAt = LocalDateTime.now();
    }

    /**
     * 결석 상태(ABSENT)에서만 인정결석을 신청할 수 있음. EXCUSED_PENDING 상태로 바뀌며, 이후 관리자가 approve/reject 처리
     */
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

    /**
     * 관리자 직접 상태 변경용 권한 설정 차후 결정 *_PENDING 상태로는 이 메서드로 변경 안됨.
     */
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

    private void validateConfirmerId(Long confirmerId) {
        if (confirmerId == null || confirmerId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 승인자 ID입니다");
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
     * 출석 기록 ID 타입안정성 때매 별도 구현
     */
    public record AttendanceRecordId(long id) {
        public AttendanceRecordId {
            if (id <= 0) {
                throw new IllegalArgumentException("ID는 양수여야 합니다.");
            }
        }
    }
}
