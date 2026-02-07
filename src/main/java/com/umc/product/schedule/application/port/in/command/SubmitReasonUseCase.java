package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.application.port.in.command.dto.SubmitReasonCommand;
import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;

/**
 * 사유 제출 출석 UseCase
 * <p>
 * 위치 인증이 어려운 경우 사유를 제출하여 출석 체크를 요청합니다.
 * 출석 기록은 EXCUSED_PENDING 상태로 생성되며, 관리자 승인을 거쳐 확정됩니다.
 */
public interface SubmitReasonUseCase {
    /**
     * 사유를 제출하여 출석 체크를 요청합니다.
     *
     * @param command 사유 제출 정보
     * @return 생성된 출석 기록 ID
     */
    AttendanceRecordId submitReason(SubmitReasonCommand command);
}
