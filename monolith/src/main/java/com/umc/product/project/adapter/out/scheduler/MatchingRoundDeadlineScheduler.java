package com.umc.product.project.adapter.out.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.umc.product.global.observability.ScheduledTaskTracer;
import com.umc.product.project.adapter.in.scheduler.MatchingResultPublicationHandler;
import com.umc.product.project.adapter.in.scheduler.MatchingRoundDeadlineHandler;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.ScheduleMatchingRoundDeadlinePort;
import com.umc.product.project.domain.ProjectMatchingRound;

import jakarta.annotation.PostConstruct;

/**
 * {@link ScheduleMatchingRoundDeadlinePort} 의 driven adapter 구현.
 * <p>
 * 매칭 차수 lifecycle ({@code create} / {@code update} / {@code delete}) 호출에 맞춰
 * {@link TaskScheduler} 에 자동 선발과 결과 발표 시점의 1회성 task 를 등록한다.
 * 자동 선발은 in-side {@link MatchingRoundDeadlineHandler}, 결과 공개는
 * {@link MatchingResultPublicationHandler} 가 실행하며,
 * 본 클래스는 등록/취소/상태 보관만 담당한다.
 * <p>
 * 부팅 시 미처리 자동 선발과 결과 발표 작업을 다시 등록한다.
 * 다중 인스턴스 환경에서 동일 차수의 작업이 중복 등록되어도 service 측에서 잠금과 완료 여부를 확인한다.
 */
@Component
@ConditionalOnProperty(
    name = "scheduler.matching-round-deadline.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class MatchingRoundDeadlineScheduler implements ScheduleMatchingRoundDeadlinePort {

    /**
     * 자동 선발 발화 시점을 deadline 정각이 아닌 buffer 만큼 뒤로 미룬다.
     * <p>
     * - 클럭 정밀도: deadline 정각에 발화 시 {@code isDecisionDeadlinePassed} 가 false 가 돼 NOT_FINALIZABLE 로 종료될 위험 회피
     * - PM 마지막 토글 커밋과의 짧은 race 보호
     * - 다른 도메인 자정 cron 부하와 분리
     */
    private final Duration deadlineBuffer;

    private final TaskScheduler taskScheduler;
    private final MatchingRoundDeadlineHandler handler;
    private final MatchingResultPublicationHandler publicationHandler;
    private final LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    private final ScheduledTaskTracer scheduledTaskTracer;

    private final Map<Long, ScheduledFuture<?>> pendingTasks = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledFuture<?>> publicationTasks = new ConcurrentHashMap<>();

    public MatchingRoundDeadlineScheduler(
        @Qualifier("matchingDeadlineTaskScheduler") TaskScheduler taskScheduler,
        MatchingRoundDeadlineHandler handler,
        LoadProjectMatchingRoundPort loadProjectMatchingRoundPort,
        MatchingRoundDeadlineSchedulerProperties properties,
        ScheduledTaskTracer scheduledTaskTracer,
        MatchingResultPublicationHandler publicationHandler
    ) {
        this.taskScheduler = taskScheduler;
        this.handler = handler;
        this.publicationHandler = publicationHandler;
        this.loadProjectMatchingRoundPort = loadProjectMatchingRoundPort;
        this.deadlineBuffer = properties.deadlineBuffer();
        this.scheduledTaskTracer = scheduledTaskTracer;
    }

    @PostConstruct
    public void recoverPendingTasks() {
        loadProjectMatchingRoundPort.listAllNotAutoDecided().forEach(this::schedule);
        loadProjectMatchingRoundPort.listAllNotPublished().forEach(this::schedulePublication);
    }

    @Override
    public void schedule(ProjectMatchingRound round) {
        Long roundId = round.getId();
        cancel(roundId);

        // 4차는 시작 시각에 랜덤 배정하고, 일반 차수는 PM 마감 이후 자동 선발한다.
        Instant runAt = round.isRandomMatching() ? round.getStartsAt() : round.getDecisionDeadline().plus(deadlineBuffer);
        // 동적 등록 task 는 @Scheduled 가 아니라 Spring 의 관측 span 이 붙지 않는다.
        // 실행 시 새 root span 을 열어 마감 자동 선발 처리를 하나의 traceId 로 묶는다.
        ScheduledFuture<?> future = taskScheduler.schedule(
            scheduledTaskTracer.trace("matchingRoundDeadline", () -> {
                try {
                    handler.handle(roundId);
                } finally {
                    pendingTasks.remove(roundId);
                }
            }),
            runAt
        );
        pendingTasks.put(roundId, future);
        schedulePublication(round);
    }

    /**
     * 결과 발표 시각에 1회성 task 를 등록한다. 같은 차수의 기존 발표 예약은 취소한다.
     */
    public void schedulePublication(ProjectMatchingRound round) {
        ScheduledFuture<?> previous = publicationTasks.remove(round.getId());
        if (previous != null) {
            previous.cancel(false);
        }
        if (round.getResultPublishAt() != null && round.getResultPublishedAt() == null) {
            registerPublication(round.getId(), round.getResultPublishAt());
        }
    }

    private void registerPublication(Long roundId, Instant runAt) {
        ScheduledFuture<?> future = taskScheduler.schedule(
            scheduledTaskTracer.trace("matchingResultPublication", () -> {
                try {
                    publicationHandler.handle(roundId);
                } finally {
                    publicationTasks.remove(roundId);
                }
            }),
            runAt
        );
        publicationTasks.put(roundId, future);
    }

    @Override
    public void cancel(Long roundId) {
        ScheduledFuture<?> publication = publicationTasks.remove(roundId);
        if (publication != null) {
            publication.cancel(false);
        }
        ScheduledFuture<?> future = pendingTasks.remove(roundId);
        if (future != null) {
            future.cancel(false);
        }
    }

    /** 테스트 용 — 현재 등록된 task 가 있는지 확인. */
    public boolean isScheduled(Long roundId) {
        return pendingTasks.containsKey(roundId);
    }
}
