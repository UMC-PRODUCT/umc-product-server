package com.umc.product.schedule.domain.enums;

import com.umc.product.schedule.domain.AttendancePolicy;
import com.umc.product.schedule.domain.ScheduleParticipantAttendance;
import lombok.Getter;

/**
 * 출석 상태를 나타냅니다.
 * <p>
 * {@link AttendancePolicy}에 따라서 {@link ScheduleParticipantAttendance} 생성 시에 그 상태가 결정됩니다.
 * <p>
 * (1) 처음 사용자가 요청했을 때 지정되는 상태입니다.
 * <p>
 * - PRESENT_PENDING: 출석 가능한 시간 내인 경우
 * <p>
 * - LATE_PENDING: 지각으로 인정되는 시간 내인 경우
 * <p>
 * - EXCUSED_PENDING: 기존 출석 기록이 없는 상태에서 사유 제출을 한 경우
 * <p>
 * - ABSENT: 지각 인정 시간 이후에 제출을 한 경우
 * <p>
 * (2) (1)단계에서 전이가 가능한 상태입니다.
 * <p>
 * - PRESENT: 출석으로 확정된 상태입니다. (PRESENT_PENDING에서 승인된 경우)
 * <p>
 * - LATE: 지각으로 확정된 상태입니다. (LATE_PENDING에서 승인된 경우)
 * <p>
 * - EXCUSED: 출석 인정된 상태입니다. (ABSENT에서 사유를 제출한 경우, EXCUSED_PENDING에서 승인된 경우)
 * <p>
 * (3) (2)단계에서 추가로 전이가 가능한 상태입니다.
 * <p>
 * - ABSENT_EXCUSE_PENDING: ABSENT 상태에서 사유를 제출하여 EXCUSED로 추가 전이가 가능합니다.
 * <p>
 * - LATE_EXCUSE_PENDING: LATE 상태에서 사유를 제출하여 EXCUSED로 추가 전이가 가능합니다.
 */
@Getter
public enum AttendanceStatus {
    // 경운 to 세은: PENDING 삭제했습니다.

    // Attendance 기록이 생성되었을 때는 아래의 값 중 하나로 시작함.
    PRESENT_PENDING(true),  // 출석으로 체크했으나 관리자 승인 대기 중
    LATE_PENDING(true),     // 지각으로 체크했으나 관리자 승인 대기 중
    EXCUSED_PENDING(true),  // 인정결석 신청 후 관리자 승인 대기 중
    ABSENT(false),          // 결석 확정 (시간 초과 또는 승인 반려)

    // 1차 상태에서 전이된 최종 상태
    PRESENT(false),         // 출석 확정
    LATE(false),            // 지각 확정
    EXCUSED(false),         // 출석 인정됨, 결석인 경우에는 다시 전이가 가능함.

    ABSENT_EXCUSE_PENDING(true),
    LATE_EXCUSE_PENDING(true),
    ;

    private final boolean isPending;

    AttendanceStatus(boolean isPending) {
        this.isPending = isPending;
    }

}
