package com.umc.product.project.adapter.out.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.umc.product.global.observability.ScheduledTaskTracer;
import com.umc.product.project.adapter.in.scheduler.MatchingResultPublicationHandler;
import com.umc.product.project.adapter.in.scheduler.MatchingRoundDeadlineHandler;
import com.umc.product.project.adapter.out.scheduler.RandomMatchingSchedulingAdapter.RandomMatchingScheduleRequested;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.ScheduleMatchingRoundDeadlinePort;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link ScheduleMatchingRoundDeadlinePort} 의 driven adapter 구현.
 * <p>
 * 매칭 차수 lifecycle ({@code create} / {@code update} / {@code delete}) 호출에 맞춰
 * {@link TaskScheduler} 에 자동 선발과 결과 발표 시점의 1회성 task 를 등록한다.
 * 자동 선발은 {@link MatchingRoundDeadlineHandler}, 결과 발표는 {@link MatchingResultPublicationHandler} 가 실행하며,
 * 본 클래스는 등록/취소/상태 보관만 담당한다.
 * <p>
 * 부팅 시 아직 처리되지 않은 자동 선발과 결과 발표 작업을 다시 등록한다.
 * 다중 인스턴스에서 같은 작업을 실행해도 service 에서 완료 여부를 확인해 중복 처리하지 않는다.
 */
@Component
@Slf4j
@ConditionalOnProperty(
    name = "scheduler.matching-round-deadline.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class MatchingRoundDeadlineScheduler implements ScheduleMatchingRoundDeadlinePort {

    /**
     * 1~3차는 결정 마감, 랜덤 매칭은 3차 결과 공개 완료 시각에서 buffer 만큼 뒤로 미룬다.
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

    // 공개 후 랜덤 매칭을 예약하는 이벤트 리스너가 등록된 뒤 미처리 작업을 복구한다.
    @EventListener(ApplicationReadyEvent.class)
    public void recoverPendingTasks() {
        loadProjectMatchingRoundPort.listAllNotAutoDecided().forEach(this::schedule);
        loadProjectMatchingRoundPort.listAllNotPublished().forEach(this::schedulePublication);
    }

    /**
     * 3차 결과 공개 트랜잭션이 커밋된 뒤 랜덤 매칭을 예약한다.
     * 예약 실패는 로그로 남기며, 미처리 작업은 기존처럼 서버 재시작 시 복구한다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void scheduleAfterThirdPublication(RandomMatchingScheduleRequested event) {
        try {
            loadProjectMatchingRoundPort.listByChapterId(event.chapterId()).stream()
                .filter(round -> round.getType() == event.matchingType() && round.isRandomMatching())
                .filter(round -> !round.isAutoDecisionExecuted())
                .forEach(this::schedule);
        } catch (RuntimeException e) {
            log.error("랜덤 매칭 예약 실패: chapterId={}, matchingType={}", event.chapterId(), event.matchingType(), e);
        }
    }

    @Override
    public void schedule(ProjectMatchingRound round) {
        Long roundId = round.getId();
        cancel(roundId);

        if (round.isAutoDecisionExecuted()) {
            schedulePublication(round);
            return;
        }
        // 랜덤 매칭은 3차 결과 공개 완료, 일반 차수는 PM 마감을 기준으로 같은 buffer를 적용한다.
        Instant baseAt = round.isRandomMatching()
            ? findThirdPublishedAt(round).orElse(null) : round.getDecisionDeadline();
        if (baseAt == null) {
            schedulePublication(round);
            return;
        }
        Instant runAt = baseAt.plus(deadlineBuffer);
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
     * 발표 시각에 공개 작업을 예약한다. 기존 예약이 있으면 교체한다.
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

    private Optional<Instant> findThirdPublishedAt(ProjectMatchingRound round) {
        Instant now = Instant.now();
        return loadProjectMatchingRoundPort.listByChapterId(round.getChapterId()).stream()
            .filter(previous -> previous.getType() == round.getType() && previous.getPhase() == MatchingPhase.THIRD
                && previous.isAutoDecisionExecuted() && previous.isResultPublishedAt(now))
            // 발표 시각이 없는 기존 차수는 자동 선발과 동시에 공개한다.
            .map(previous -> previous.getResultPublishedAt() != null
                ? previous.getResultPublishedAt() : previous.getAutoDecisionExecutedAt())
            .findFirst();
    }

}
