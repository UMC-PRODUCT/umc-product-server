package com.umc.product.project.application.port.in.command;

/**
 * 매칭 결과 발표 처리.
 */
public interface PublishProjectMatchingResultUseCase {

    /**
     * 해당 차수의 지원서 합불과 팀원 활성화를 반영하고 공개 완료 시각을 기록합니다.
     *
     * @return 공개를 완료했으면 true, 이미 공개했거나 공개 조건을 만족하지 않으면 false
     */
    boolean publish(Long matchingRoundId);
}
