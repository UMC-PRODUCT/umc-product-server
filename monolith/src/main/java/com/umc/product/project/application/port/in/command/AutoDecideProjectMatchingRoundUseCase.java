package com.umc.product.project.application.port.in.command;

/**
 * 매칭 차수 자동 선발 UseCase (MATCHING-201 + 스케줄러 공통 진입점).
 * <p>
 * 1~3차는 결정 마감 후 정책 매트릭스 기반으로 지원자를 선발하고, 랜덤 매칭은 시작 시각부터 실행합니다.
 * 발표 시각이 설정된 차수는 PENDING_PUBLICATION 멤버를 생성하며 지원서 합불은 발표 시 반영합니다.
 * <p>
 * 호출자:
 * <ul>
 *   <li>운영진 수동: {@code POST /api/v1/project/matching-rounds/{id}/auto-decide}</li>
 *   <li>스케줄러: {@code MatchingRoundDeadlineScheduler} (1~3차 마감+buffer, 랜덤 매칭 시작 시각)</li>
 * </ul>
 */
public interface AutoDecideProjectMatchingRoundUseCase {

    /**
     * @param matchingRoundId    자동 선발 대상 매칭 차수 ID
     * @param executedByMemberId 운영진 수동 호출 시 본인 ID, 스케줄러 호출 시 {@code null}
     */
    void autoDecide(Long matchingRoundId, Long executedByMemberId);
}
