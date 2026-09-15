package com.umc.product.project.application.port.in.command;

/**
 * 매칭 차수 자동 선발 UseCase (MATCHING-201 + 스케줄러 공통 진입점).
 * <p>
 * 차수 종료 시점에 정책 매트릭스 기반으로 SUBMITTED 지원자를 자동 보충하고
 * 합격자에 대해 ProjectMember 자동 추가까지 수행합니다.
 * <p>
 * 호출자:
 * <ul>
 *   <li>운영진 수동: {@code POST /api/v1/project/matching-rounds/{id}/auto-decide}</li>
 *   <li>스케줄러: {@code MatchingRoundDeadlineRegistry} (deadline 시점 자동 트리거)</li>
 * </ul>
 */
public interface AutoDecideProjectMatchingRoundUseCase {

    /**
     * @param matchingRoundId    자동 선발 대상 매칭 차수 ID
     * @param executedByMemberId 운영진 수동 호출 시 본인 ID, 스케줄러 호출 시 {@code null}
     */
    void autoDecide(Long matchingRoundId, Long executedByMemberId);
}
