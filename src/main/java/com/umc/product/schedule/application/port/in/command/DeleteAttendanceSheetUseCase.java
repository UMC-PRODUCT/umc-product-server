package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;

/**
 * 출석부 물리 삭제 UseCase.
 * <p>
 * 출석 기록 보존이 필요한 경우 삭제 대신 UpdateAttendanceSheetUseCase.deactivate()를 사용할 것. 해당 부분은 데이터 까지도 전부 삭제되는 경우를 위해 필요
 */
public interface DeleteAttendanceSheetUseCase {

    void delete(AttendanceSheetId sheetId);
}
