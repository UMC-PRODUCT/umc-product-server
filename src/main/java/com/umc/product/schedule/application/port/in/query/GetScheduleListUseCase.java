package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.ScheduleWithStatsInfo;
import java.util.List;

public interface GetScheduleListUseCase {

    /**
     * - 1. 스케줄 목록 조회 - 진행 중인 것 우선 - 이후 종료된 것 최신순
     * - 2. 특정 스케줄의 출석 통계 조회도 함께 조회
     * - 3. 중앙 운영진은 기수 전체 일정 조회, 학교 운영진은 본인 생성 일정만 조회
     *
     * @param memberId 현재 로그인한 회원 ID
     */
    List<ScheduleWithStatsInfo> getAll(Long memberId);
}
