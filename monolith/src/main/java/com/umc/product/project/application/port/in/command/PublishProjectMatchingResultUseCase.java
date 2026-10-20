package com.umc.product.project.application.port.in.command;

/**
 * 매칭 결과 공개 UseCase (결과 발표 스케줄러 진입점).
 */
public interface PublishProjectMatchingResultUseCase {

    /**
     * 해당 차수의 공개 대기 멤버를 활성화하고 결과 공개를 완료합니다.
     *
     * @param matchingRoundId 결과 공개 대상 매칭 차수 ID
     */
    void publish(Long matchingRoundId);
}
