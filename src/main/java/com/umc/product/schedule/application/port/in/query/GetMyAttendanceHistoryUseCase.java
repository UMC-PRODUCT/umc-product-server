package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.MyAttendanceHistoryInfo;
import java.util.List;

/**
 * 개인 전체 출석 이력을 조회하는 UseCase. 이건 필터링 기준 없이 그냥 쭉 이력 반환 출석 기록 → 출석부 → 일정을 역추적하여 일정명, 일시, 출석 상태를 결합한 이력을 반환 최신순 정렬.
 */
public interface GetMyAttendanceHistoryUseCase {

    List<MyAttendanceHistoryInfo> getHistory(Long memberId);
}
