package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.statistics.MatchingStatisticsInfo;

/**
 * 매칭통계 조회 UseCase.
 * <ul>
 *   <li>{@link #getManagerStats} — 운영진: 지부 전체 매칭통계 (PROJECT-STAT-003)</li>
 * </ul>
 * <p>
 * 집계 대상: ACTIVE ProjectMember 중 application != null(지원서 경로) 기준.
 * application 이 null 인 랜덤 매칭 멤버는 차수 정보가 없으므로 제외.
 */
public interface GetMatchingStatisticsUseCase {

    /**
     * 운영진: gisuId + chapterId 범위 내 매칭통계를 집계합니다.
     *
     * @param gisuId    기수 ID
     * @param chapterId 지부 ID
     */
    MatchingStatisticsInfo getManagerStats(Long gisuId, Long chapterId);
}
