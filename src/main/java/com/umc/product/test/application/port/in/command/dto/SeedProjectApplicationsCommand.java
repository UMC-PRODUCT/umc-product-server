package com.umc.product.test.application.port.in.command.dto;

/**
 * 지원서 시나리오 시딩 Command.
 * <p>
 * 지정 매칭 차수와 지부를 기준으로, 아직 팀에 합류하지 않은 ACTIVE 챌린저들이
 * 랜덤 프로젝트에 지원서를 제출하고 합불 결정까지 완료하는 시나리오를 실행한다.
 *
 * @param matchingRoundId 지원할 매칭 차수 ID. 반드시 현재 OPEN 상태여야 한다.
 * @param chapterId       대상 지부 ID. 해당 지부의 IN_PROGRESS 프로젝트가 지원 대상이 된다.
 * @param approveRatio    SUBMITTED 지원서 중 APPROVED 처리할 비율 (0.0 ~ 1.0, 기본값 0.5).
 *                        나머지는 REJECTED 처리된다.
 */
public record SeedProjectApplicationsCommand(
    Long matchingRoundId,
    Long chapterId,
    Double approveRatio
) {

}
