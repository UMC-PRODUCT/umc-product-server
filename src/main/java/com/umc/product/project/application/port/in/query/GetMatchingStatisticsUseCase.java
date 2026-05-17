package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.statistics.MatchingStatisticsInfo;

/**
 * 매칭통계 조회 UseCase.
 * <p>
 * 집계 대상: ACTIVE ProjectMember 중 application != null(지원서 경로) 기준. application 이 null 인 랜덤 매칭 멤버는 차수 정보가 없으므로 제외 (UI상
 * 랜덤매칭에 대한 통계 부재)
 * <p>
 * 호출자 역할에 따라 내부에서 scope를 분기한다.
 * <ul>
 *   <li>ADMIN(운영진) → gisuId + chapterId 범위 전체 집계 (PROJECT-STAT-003)</li>
 *   <li>일반 챌린저 → callerMemberId 소유 프로젝트로 scope 제한 (PROJECT-STAT-004)</li>
 * </ul>
 */
public interface GetMatchingStatisticsUseCase {

    /**
     * @param gisuId         기수 ID
     * @param chapterId      지부 ID
     * @param callerMemberId 요청자 memberId (역할 판단 및 PM챌린저 scope 제한에 사용)
     */
    MatchingStatisticsInfo getStats(Long gisuId, Long chapterId, Long callerMemberId);
}
