package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.application.port.in.command.dto.CreateAttendanceSheetCommand;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;

/**
 * 일정(Schedule)에 연결되는 출석부(AttendanceSheet)를 생성하는 Facade 파트.
 * <p>
 * 출석부 생성 시 출석 가능 시간대(AttendanceWindow)와 승인 필요 여부(requiresApproval)를 설정
 * <p> 일정 1개에 출석부 1개가 1:1로 연결 -> 현재 카테고리별 차이 존재 X
 * <p> 일정과 출석부를 동시에 생성하려면 CreateScheduleWithAttendanceUseCase(Facade)를 사용.
 */
public interface CreateAttendanceSheetUseCase {

    AttendanceSheetId create(CreateAttendanceSheetCommand command);
}
