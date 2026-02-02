package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.application.port.in.command.dto.UpdateAttendanceSheetCommand;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;

/**
 * 출석부의 시간대/승인정책 수정 및 활성화/비활성화를 처리하는 UseCase.
 * <p>
 * 비활성화(deactivate)는 물리 삭제 대신 사용하며, 기존 출석 기록을 보존 비활성 상태에서는 출석 체크가 불가능함.
 */
public interface UpdateAttendanceSheetUseCase {

    /**
     * 출석 시간대(window)와 승인 모드(requiresApproval)를 수정
     */
    void update(UpdateAttendanceSheetCommand command);

    /**
     * 출석부 비활성화. 비활성 상태에서는 출석 체크 불가.
     */
    void deactivate(AttendanceSheetId sheetId);

    /**
     * 비활성화된 출석부를 다시 활성화.
     */
    void activate(AttendanceSheetId sheetId);
}
