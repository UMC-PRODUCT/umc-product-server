package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.MyAttendanceHistorySummary;
import java.util.List;

public interface GetMyAttendanceHistoryUseCase {

    /**
     * 나의 출석 현황 조회 (히스토리) - 과거 및 현재 출석 기록 - 주차별 오름차순 정렬
     */
    List<MyAttendanceHistorySummary> getHistory(Long memberId);
}
