package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.PendingAttendanceInfo;
import java.util.List;

/**
 * 운영진용임 특정 일정에서 승인 대기 중인(PENDING) 출석 요청 목록을 조회하는 UseCase.
 * <p>
 * 일정 ID로 출석부를 찾고, 해당 출석부의 PENDING 상태 기록을 멤버 정보와 함께 반환 운영진이 approve/reject 처리할 대상을 확인하기 위해 사용 조회된 기록의 승인/반려는
 * ApproveAttendanceUseCase에서 처리.
 */
public interface GetPendingAttendancesUseCase {

    List<PendingAttendanceInfo> getPendingList(Long scheduleId);
}
