package com.umc.product.project.application.port.out;

import com.umc.product.project.domain.ProjectMatchingRound;

/**
 * 매칭 차수의 자동 선발과 결과 발표를 예약하는 동적 스케줄링 Port.
 * <p>
 * 매칭 차수 lifecycle ({@code create} / {@code update} / {@code delete}) 에 맞춰 호출되어
 * 1~3차는 결정 마감, 랜덤 매칭은 같은 지부·유형의 3차 결과 공개 완료 시각에 buffer(기본 1분)를 더해 task 를 등록한다.
 * 발표 시각이 설정된 차수는 공개 task 도 별도로 등록한다.
 */
public interface ScheduleMatchingRoundDeadlinePort {

    /**
     * 자동 선발과 결과 발표를 각각 1회 실행하도록 등록한다.
     * 동일 ID 의 등록이 이미 있다면 취소 후 재등록한다.
     */
    void schedule(ProjectMatchingRound round);

    /**
     * 등록된 task 를 취소한다. 등록되지 않은 ID 는 no-op.
     */
    void cancel(Long roundId);
}
