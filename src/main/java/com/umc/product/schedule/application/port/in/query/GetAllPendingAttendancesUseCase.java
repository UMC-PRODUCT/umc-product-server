package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.PendingAttendancesByScheduleInfo;
import java.util.List;

/**
 * 운영진용 - 기수 전체의 승인 대기 중인 출석 기록을 일정별로 그룹핑하여 조회하는 UseCase.
 * <p>
 * 기존 API는 일정별로 개별 호출해야 했으나, 이 API는 한 번에 전체 조회 가능.
 * <p>
 * 조회 대상: - 해당 기수의 활성 출석부에 속한 승인 대기 기록 - PRESENT_PENDING, LATE_PENDING, EXCUSED_PENDING 상태
 * <p>
 * 반환 형식: - scheduleId별로 그룹핑 - 각 그룹에 일정명과 승인 대기 목록 포함
 */
public interface GetAllPendingAttendancesUseCase {

    /**
     * 기수 전체의 승인 대기 출석 목록을 일정별로 조회
     *
     * @param gisuId 기수 ID
     * @return 일정별로 그룹핑된 승인 대기 출석 목록
     */
    List<PendingAttendancesByScheduleInfo> getAllPendingList(Long gisuId);
}
