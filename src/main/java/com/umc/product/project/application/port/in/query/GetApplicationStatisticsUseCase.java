package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.statistics.ApplicationStatisticsInfo;

/**
 * 지원통계 조회 UseCase.
 * <p>
 * 호출자 역할에 따라 내부에서 scope를 분기한다.
 * <ul>
 *   <li>ADMIN(운영진) → gisuId + chapterId 범위 전체 집계 (PROJECT-STAT-001)</li>
 *   <li>일반 챌린저 → callerMemberId 소유 프로젝트로 scope 제한 (PROJECT-STAT-002)</li>
 * </ul>
 */
public interface GetApplicationStatisticsUseCase {

    /**
     * @param gisuId          기수 ID
     * @param chapterId       지부 ID
     * @param callerMemberId  요청자 memberId (역할 판단 및 PM챌린저 scope 제한에 사용)
     */
    ApplicationStatisticsInfo getStats(Long gisuId, Long chapterId, Long callerMemberId);
}
