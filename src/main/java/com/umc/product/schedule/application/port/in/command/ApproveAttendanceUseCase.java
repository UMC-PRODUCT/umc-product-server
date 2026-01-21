package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;

/**
 * 출석 승인/반려 UseCase
 */
public interface ApproveAttendanceUseCase {

    /**
     * 출석 승인
     *
     * @param recordId    출석 기록 ID
     * @param confirmerId 승인자(운영진) 챌린저 ID
     */
    void approve(AttendanceRecordId recordId, Long confirmerId);

    /**
     * 출석 반려
     *
     * @param recordId    출석 기록 ID
     * @param confirmerId 승인자(운영진) 챌린저 ID
     */
    void reject(AttendanceRecordId recordId, Long confirmerId);
}
