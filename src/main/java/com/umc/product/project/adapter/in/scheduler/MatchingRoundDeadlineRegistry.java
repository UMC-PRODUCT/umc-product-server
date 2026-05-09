package com.umc.product.project.adapter.in.scheduler;

import com.umc.product.project.application.port.in.command.AutoDecideProjectMatchingRoundUseCase;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.ScheduleMatchingRoundDeadlinePort;
import com.umc.product.project.domain.ProjectMatchingRound;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

/**
 * 매칭 차수 종료 시점에 {@link AutoDecideProjectMatchingRoundUseCase} 를 자동으로 호출하는 동적 스케줄러.
 * <p>
 * 매칭 차수 lifecycle 메서드({@code create} / {@code update} / {@code delete}) 가 호출하며,
 * 부팅 시점에 미처리 round 를 모두 재등록하여 JVM 재시작에도 안전하다.
 * <p>
 * 매일 polling 하지 않고 정확히 {@code decisionDeadline} 시점에만 1회 실행되므로 불필요한 자원 소비가 없다.
 * 다중 인스턴스 환경에서 동시 등록되어도 서비스 측 멱등성({@code autoDecisionExecutedAt != null}) 으로 방어된다.
 * <p>
 * {@code scheduler.matching-round-deadline.enabled=false} 로 비활성화 가능 (default 활성).
 */
@Component
@ConditionalOnProperty(
    name = "scheduler.matching-round-deadline.enabled",
    havingValue = "true",
    matchIfMissing = true
)
@Slf4j
public class MatchingRoundDeadlineRegistry implements ScheduleMatchingRoundDeadlinePort {

    private final TaskScheduler taskScheduler;
    private final AutoDecideProjectMatchingRoundUseCase autoDecideUseCase;
    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;

    private final Map<Long, ScheduledFuture<?>> pendingTasks = new ConcurrentHashMap<>();

    public MatchingRoundDeadlineRegistry(
        @Qualifier("matchingDeadlineTaskScheduler") TaskScheduler taskScheduler,
        @Lazy AutoDecideProjectMatchingRoundUseCase autoDecideUseCase,
        LoadProjectMatchingRoundPort loadProjectMatchingRoundPort
    ) {
        this.taskScheduler = taskScheduler;
        this.autoDecideUseCase = autoDecideUseCase;
        this.loadProjectMatchingRoundPort = loadProjectMatchingRoundPort;
    }

    /**
     * 부팅 시 자동 선발이 아직 실행되지 않은 모든 매칭 차수를 재등록한다.
     * deadline 이 이미 경과한 round 는 {@link TaskScheduler} 가 즉시 실행한다.
     */
    @PostConstruct
    public void recoverPendingTasks() {
        loadProjectMatchingRoundPort.listAllNotAutoDecided().forEach(this::schedule);
    }

    /**
     * 매칭 차수의 결정 마감 시점에 자동 선발을 1회 실행하도록 등록한다.
     * 기존 등록이 있다면 취소 후 재등록한다 ({@code update} 시 사용).
     */
    public void schedule(ProjectMatchingRound round) {
        Long roundId = round.getId();
        cancel(roundId);

        ScheduledFuture<?> future = taskScheduler.schedule(
            () -> runAutoDecide(roundId),
            round.getDecisionDeadline()
        );
        pendingTasks.put(roundId, future);
    }

    /**
     * 매칭 차수 삭제 시 등록된 task 를 취소한다.
     */
    public void cancel(Long roundId) {
        ScheduledFuture<?> future = pendingTasks.remove(roundId);
        if (future != null) {
            future.cancel(false);
        }
    }

    /** 테스트 용 — 현재 등록된 task 가 있는지 확인. */
    public boolean isScheduled(Long roundId) {
        return pendingTasks.containsKey(roundId);
    }

    private void runAutoDecide(Long roundId) {
        try {
            autoDecideUseCase.autoDecide(roundId, null);
        } catch (Exception e) {
            log.error("Failed to auto-decide matching round {}", roundId, e);
        } finally {
            pendingTasks.remove(roundId);
        }
    }
}
