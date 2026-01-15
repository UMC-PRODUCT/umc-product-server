package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.PendingAttendanceInfo;
import java.util.List;

public interface GetPendingAttendancesUseCase {

    /**
     * 승인 대기 중인 출석 요청 목록 조회
     */
    List<PendingAttendanceInfo> getPendingList(Long scheduleId);
}
