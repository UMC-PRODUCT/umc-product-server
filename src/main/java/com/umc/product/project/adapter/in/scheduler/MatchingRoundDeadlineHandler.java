package com.umc.product.project.adapter.in.scheduler;

import com.umc.product.project.application.port.in.command.AutoDecideProjectMatchingRoundUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 매칭 차수 결정 마감 시점에 트리거되어 자동 선발 UseCase 를 호출하는 driving adapter.
 * <p>
 * out-side {@link com.umc.product.project.adapter.out.scheduler.MatchingRoundDeadlineScheduler}
 * 가 {@link org.springframework.scheduling.TaskScheduler} 에 본 컴포넌트의 {@link #handle(Long)} 를 묶어 등록한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingRoundDeadlineHandler {

    private final AutoDecideProjectMatchingRoundUseCase autoDecideUseCase;

    /**
     * deadline 시점에 1회 호출되어 자동 선발을 실행한다. 예외는 swallow 하여 다음 task 진행을 막지 않는다.
     */
    public void handle(Long matchingRoundId) {
        try {
            autoDecideUseCase.autoDecide(matchingRoundId, null);
        } catch (Exception e) {
            log.error("Failed to auto-decide matching round {}", matchingRoundId, e);
        }
    }
}
