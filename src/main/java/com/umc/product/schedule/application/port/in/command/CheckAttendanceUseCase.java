package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.application.port.in.command.dto.CheckAttendanceCommand;

/**
 * 멤버가 출석 체크를 수행하는 UseCase.
 * <p>
 * 호출 시점의 시각을 기준으로 AttendanceWindow가 출석/지각/결석을 자동 판정 출석부의 requiresApproval 설정에 따라 즉시 확정되거나 PENDING 상태로 생성
 * <p>
 * 전제 조건<p> - 출석부가 활성(active) 상태여야 한다<p> - 해당 멤버의 출석 기록(AttendanceRecord)이 이미 존재해야 한다 <p> - 아직 체크인하지 않은 상태(checkedAt ==
 * null)여야한다
 */
public interface CheckAttendanceUseCase {

    /**
     * @param command 출석부 ID, 멤버 ID, 체크인 시각을 포함
     * @return 생성된 출석 기록 ID
     * @throws com.umc.product.global.exception.BusinessException 출석부가 비활성화 상태이거나 출석 시간이 아닌 경우
     */
    Long check(CheckAttendanceCommand command);
}
