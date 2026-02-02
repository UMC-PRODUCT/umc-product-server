package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.AvailableAttendanceInfo;
import java.util.List;

/**
 * 챌린저가 현재 출석 체크할 수 있는 일정 목록을 조회하는 UseCase.
 * <p>
 * 활성 출석부 중 아직 종료되지 않은 일정만 필터링. 조회 시작 시간 오름차순 정렬.
 */
public interface GetAvailableAttendancesUseCase {

    List<AvailableAttendanceInfo> getAvailableList(Long memberId);
}
