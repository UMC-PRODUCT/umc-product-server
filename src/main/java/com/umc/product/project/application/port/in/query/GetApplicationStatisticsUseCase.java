package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.statistics.ApplicationStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.MyApplicationStatisticsInfo;

/**
 * 지원통계 조회 UseCase.
 * <ul>
 *   <li>{@link #getManagerStats} — 운영진: 지부 전체 지원통계 (PROJECT-STAT-001)</li>
 *   <li>{@link #getMyStats} — PM챌린저: 본인 프로젝트 지원통계 (PROJECT-STAT-002)</li>
 * </ul>
 */
public interface GetApplicationStatisticsUseCase {

    /**
     * 운영진: gisuId + chapterId 범위 내 지원통계를 집계합니다.
     *
     * @param gisuId    기수 ID
     * @param chapterId 지부 ID
     */
    ApplicationStatisticsInfo getManagerStats(Long gisuId, Long chapterId);

    /**
     * PM챌린저: 단일 프로젝트의 지원통계를 집계합니다.
     *
     * @param projectId 프로젝트 ID
     */
    MyApplicationStatisticsInfo getMyStats(Long projectId);
}
