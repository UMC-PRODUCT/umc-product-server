package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.MyAttendanceHistoryInfo;
import java.util.List;

/**
 * 특정 챌린저의 출석 이력을 조회하는 UseCase.
 * challengerId를 받아 해당 챌린저의 모든 기수 출석 기록을 반환.
 * 출석 기록 → 출석부 → 일정을 역추적하여 일정명, 일시, 출석 상태를 결합한 이력을 반환.
 * 최신순 정렬.
 */
public interface GetChallengerAttendanceHistoryUseCase {

    /**
     * 챌린저 ID로 출석 이력 조회
     * @param challengerId 챌린저 ID
     * @return 출석 이력 목록 (최신순)
     */
    List<MyAttendanceHistoryInfo> getHistoryByChallengerId(Long challengerId);
}
