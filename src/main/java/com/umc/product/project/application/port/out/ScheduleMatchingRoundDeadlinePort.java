package com.umc.product.project.application.port.out;

import com.umc.product.project.domain.ProjectMatchingRound;

/**
 * 매칭 차수 결정 마감 시점에 자동 선발을 트리거하는 동적 스케줄링 Port.
 * <p>
 * 매칭 차수 lifecycle ({@code create} / {@code update} / {@code delete}) 에 맞춰 호출되어
 * 결정 마감 정확한 시점에 1회 실행되는 task 를 등록한다.
 */
public interface ScheduleMatchingRoundDeadlinePort {

    /**
     * 결정 마감 시점에 자동 선발을 1회 실행하도록 등록한다.
     * 동일 ID 의 등록이 이미 있다면 취소 후 재등록한다.
     */
    void schedule(ProjectMatchingRound round);

    /**
     * 등록된 task 를 취소한다. 등록되지 않은 ID 는 no-op.
     */
    void cancel(Long roundId);
}
