package com.umc.product.project.application.port.in.command;

/**
 * 매칭 결과 발표 처리.
 */
public interface PublishProjectMatchingResultUseCase {

    /**
     * 해당 차수의 지원서 합불과 팀원 활성화를 반영하고 공개 완료 시각을 기록합니다.
     */
    void publish(Long matchingRoundId);
}
