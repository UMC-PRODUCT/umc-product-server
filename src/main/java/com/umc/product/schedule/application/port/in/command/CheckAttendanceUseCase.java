package com.umc.product.schedule.application.port.in.command;

import com.umc.product.schedule.application.port.in.command.dto.CheckAttendanceCommand;
import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;

/**
 * 챌린저가 출석 체크를 수행하는 UseCase. -> 이거 챌린저...에서 멤버로 바꿔야한다 생각
 * <p>
 * 호출 시점의 시각을 기준으로 AttendanceWindow가 출석/지각/결석을 자동 판정 출석부의 requiresApproval 설정에 따라 즉시 확정되거나 PENDING 상태로 생성
 * <p>
 * 전제 조건<p> - 출석부가 활성(active) 상태여야 한다<p> - 해당 챌린저의 출석 기록(AttendanceRecord)이 이미 존재해야 한다 <p> - 아직 체크인하지 않은 상태(checkedAt ==
 * null)여야한다
 */
public interface CheckAttendanceUseCase {

    /**
     * @param command 출석부 ID, 챌린저 ID, 체크인 시각을 포함
     * @return 체크인 처리된 출석 기록 ID
     */
    AttendanceRecordId check(CheckAttendanceCommand command);
}
