package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.AvailableAttendanceInfo;
import java.util.List;

public interface GetAvailableAttendancesUseCase {

    /**
     * 출석 가능한 세션 목록 조회 (멤버용) - 현재 진행 중이거나 곧 시작하는 세션 - 아직 출석 처리가 완료되지 않은 것
     */
    List<AvailableAttendanceInfo> getAvailableList(Long memberId);
}
