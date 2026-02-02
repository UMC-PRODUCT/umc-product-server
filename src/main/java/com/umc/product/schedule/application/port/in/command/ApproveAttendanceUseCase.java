package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;

/**
 * 운영진이 PENDING 상태의 출석 기록을 승인 또는 반려하는 UseCase.
 * <p>
 * 출석부의 requiresApproval=true일 때 체크인하면 PRESENT_PENDING / LATE_PENDING 상태로 생성되는데, 이 UseCase를 통해 최종 확정 처리함
 * <p>
 * 상태가 approve일떄 PRESENT_PENDING → PRESENT, LATE_PENDING → LATE, EXCUSED_PENDING → EXCUSED
 * <p>- reject일때 모든 *_PENDING → ABSENT
 */
public interface ApproveAttendanceUseCase {

    /**
     * @param recordId    승인할 출석 기록 ID (PENDING 상태여야 함)
     * @param confirmerId 승인자 ID - 승인 이력에 누가 했는지 기록됨
     */
    void approve(AttendanceRecordId recordId, Long confirmerId);

    /**
     * @param recordId    반려할 출석 기록 ID (PENDING 상태여야 함)
     * @param confirmerId 반려자 ID - 반려 이력에 기록됨 위와 같음
     */
    void reject(AttendanceRecordId recordId, Long confirmerId);
}
