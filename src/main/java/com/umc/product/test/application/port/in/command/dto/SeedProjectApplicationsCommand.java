package com.umc.product.test.application.port.in.command.dto;

/**
 * 지원서 시나리오 시딩 Command.
 * <p>
 * 지정 매칭 차수 + 지부를 기준으로, 아직 팀에 합류하지 않은 ACTIVE 챌린저들이 IN_PROGRESS 프로젝트에
 * 지원서를 제출한다. 각 챌린저는 도메인 시퀀스 (createDraft → fill → submit) 를 거친 뒤 최종 상태가
 * {@code SUBMITTED} / {@code APPROVED} / {@code REJECTED} 중 하나로 무작위 결정된다 (≈ 1/3 분포).
 * <p>
 * ProjectMember 등록은 시딩이 손대지 않는다 — 정상 플로우인 차수 종료 시점의 {@code autoDecide} 에서 일괄 처리된다.
 *
 * @param matchingRoundId 지원할 매칭 차수 ID. 현재 OPEN 상태여야 한다.
 * @param chapterId       대상 지부 ID. 해당 지부의 IN_PROGRESS 프로젝트가 지원 대상이 된다.
 */
public record SeedProjectApplicationsCommand(
    Long matchingRoundId,
    Long chapterId
) {

}
