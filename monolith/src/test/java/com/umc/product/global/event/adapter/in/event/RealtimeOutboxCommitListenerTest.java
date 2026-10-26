package com.umc.product.global.event.adapter.in.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.umc.product.global.config.OutboxRoutingConfig;
import com.umc.product.global.config.OutboxWorkerConfig;
import com.umc.product.global.event.adapter.in.scheduler.EventOutboxPoller;
import com.umc.product.global.event.adapter.out.OutboxCommitSignalPublisher;
import com.umc.product.global.event.application.event.RealtimeOutboxCommittedEvent;
import com.umc.product.global.event.application.port.in.RequestOutboxRunUseCase;
import com.umc.product.global.event.application.service.EventOutboxRelayMetrics;
import com.umc.product.global.event.application.service.EventOutboxRelayService;
import com.umc.product.global.event.application.service.OutboxEventRoutingPolicy;
import com.umc.product.global.event.application.service.OutboxProcessingGroup;
import com.umc.product.global.event.application.service.OutboxWorker;
import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.global.event.domain.EventOutbox;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@DisplayName("커밋 신호와 실시간 Outbox Worker 연결")
class RealtimeOutboxCommitListenerTest {

    private static final String ENABLED = "app.event-outbox.workers.realtime.immediate-dispatch-enabled=true";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(OutboxRoutingConfig.class, OutboxWorkerConfig.class, EventOutboxPoller.class,
            OutboxCommitSignalPublisher.class, RealtimeOutboxCommitListener.class)
        .withBean(EventOutboxRelayMetrics.class, () -> new EventOutboxRelayMetrics(new SimpleMeterRegistry()))
        .withBean(EventOutboxRelayService.class, () -> mock(EventOutboxRelayService.class));

    @Test
    @DisplayName("기본 설정은 즉시 처리를 등록하지 않고 기존 폴링을 유지한다")
    void 기본_설정() {
        // when / then
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(RealtimeOutboxCommitListener.class);
            assertThat(context).hasSingleBean(EventOutboxPoller.class);
            context.publishEvent(new RealtimeOutboxCommittedEvent());
            verifyNoInteractions(context.getBean(EventOutboxRelayService.class));
        });
    }

    @Test
    @DisplayName("커밋 신호는 REALTIME 전용 executor에서 기존 그룹과 배치 크기로 처리한다")
    void 실시간_Worker_연결() {
        // given
        Queue<String> threads = new ConcurrentLinkedQueue<>();

        // when
        runner.withPropertyValues(ENABLED, "app.event-outbox.workers.realtime.batch-size=7").run(context -> {
            assertThat(context).hasSingleBean(RealtimeOutboxCommitListener.class);
            var relay = context.getBean(EventOutboxRelayService.class);
            var policy = context.getBean(OutboxEventRoutingPolicy.class);
            doAnswer(invocation -> {
                threads.add(Thread.currentThread().getName());
                return 0;
            }).when(relay).relay(OutboxProcessingGroup.REALTIME, policy.selectionFor(OutboxProcessingGroup.REALTIME), 7);

            context.publishEvent(new RealtimeOutboxCommittedEvent());

            verify(relay, timeout(5000).times(3)).relay(
                OutboxProcessingGroup.REALTIME, policy.selectionFor(OutboxProcessingGroup.REALTIME), 7
            );
            verifyNoMoreInteractions(relay);
        });

        // then
        assertThat(threads).hasSize(3).allMatch(name -> name.startsWith("outbox-realtime-"));
    }

    @Test
    @DisplayName("즉시 처리만 끄면 커밋 신호는 무시하고 폴링은 같은 Worker를 실행한다")
    void 즉시_처리만_비활성() {
        // when / then
        runner.withPropertyValues("app.event-outbox.workers.realtime.immediate-dispatch-enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(RealtimeOutboxCommitListener.class);
                var relay = context.getBean(EventOutboxRelayService.class);
                var policy = context.getBean(OutboxEventRoutingPolicy.class);
                context.publishEvent(new RealtimeOutboxCommittedEvent());
                verifyNoInteractions(relay);

                context.getBean(EventOutboxPoller.class).pollRealtime();

                verify(relay, timeout(5000).times(3)).relay(
                    OutboxProcessingGroup.REALTIME, policy.selectionFor(OutboxProcessingGroup.REALTIME), 100
                );
                verifyNoMoreInteractions(relay);
            });
    }

    @Test
    @DisplayName("relay를 끄면 즉시 처리 설정과 무관하게 신호 수신기와 폴러를 모두 등록하지 않는다")
    void 전체_처리_비활성() {
        // when / then
        runner.withPropertyValues(ENABLED, "app.event-outbox.relay-enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(RealtimeOutboxCommitListener.class);
            assertThat(context).doesNotHaveBean(EventOutboxPoller.class);
            context.publishEvent(new RealtimeOutboxCommittedEvent());
            verifyNoInteractions(context.getBean(EventOutboxRelayService.class));
        });
    }

    @Test
    @DisplayName("저장 신호는 커밋 전에는 Worker를 깨우지 않고 커밋 이후 실행을 요청한다")
    void 커밋_전후_연결() {
        // when / then
        runner.withPropertyValues(ENABLED).run(context -> {
            var relay = context.getBean(EventOutboxRelayService.class);
            var policy = context.getBean(OutboxEventRoutingPolicy.class);
            TransactionSynchronizationManager.initSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(true);
            try {
                context.getBean(OutboxCommitSignalPublisher.class).requestAfterCommit(List.of(outbox()));
                verifyNoInteractions(relay);

                TransactionSynchronizationManager.getSynchronizations()
                    .forEach(TransactionSynchronization::afterCommit);

                verify(relay, timeout(5000).times(3)).relay(
                    OutboxProcessingGroup.REALTIME, policy.selectionFor(OutboxProcessingGroup.REALTIME), 100
                );
            } finally {
                TransactionSynchronizationManager.clearSynchronization();
                TransactionSynchronizationManager.setActualTransactionActive(false);
            }
        });
    }

    @Test
    @DisplayName("롤백된 저장 신호는 수신기가 활성화되어 있어도 Worker를 깨우지 않는다")
    void 롤백_연결() {
        // when / then
        runner.withPropertyValues(ENABLED).run(context -> {
            TransactionSynchronizationManager.initSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(true);
            try {
                context.getBean(OutboxCommitSignalPublisher.class).requestAfterCommit(List.of(outbox()));
                TransactionSynchronizationManager.getSynchronizations().forEach(
                    synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK)
                );
                verifyNoInteractions(context.getBean(EventOutboxRelayService.class));
            } finally {
                TransactionSynchronizationManager.clearSynchronization();
                TransactionSynchronizationManager.setActualTransactionActive(false);
            }
        });
    }

    @Test
    @DisplayName("executor 거절은 오류로 전파하지 않고 이후 폴링이 같은 Worker를 다시 실행한다")
    void 실행_거절_후_폴링_복구() {
        // given
        AtomicBoolean accepting = new AtomicBoolean(false);
        Queue<Runnable> tasks = new ArrayDeque<>();
        Executor executor = task -> {
            if (!accepting.get()) {
                throw new RejectedExecutionException("테스트 실행 거절");
            }
            tasks.add(task);
        };
        var relay = mock(EventOutboxRelayService.class);
        var policy = new OutboxRoutingConfig().outboxEventRoutingPolicy();
        var worker = new OutboxWorker(OutboxProcessingGroup.REALTIME, executor, relay, policy, 100);
        var background = mock(RequestOutboxRunUseCase.class);
        var poller = new EventOutboxPoller(worker, background);

        // when / then
        listenerRunner(worker).run(context -> {
            assertThatCode(() -> context.publishEvent(new RealtimeOutboxCommittedEvent())).doesNotThrowAnyException();
            assertThat(tasks).isEmpty();
            verifyNoInteractions(relay);

            accepting.set(true);
            poller.pollRealtime();
            assertThat(tasks).hasSize(1);
            verifyNoInteractions(relay);
            tasks.remove().run();

            verify(relay).relay(OutboxProcessingGroup.REALTIME, policy.selectionFor(OutboxProcessingGroup.REALTIME), 100);
            verifyNoInteractions(background);
        });
    }

    @Test
    @DisplayName("종료된 Worker의 실행 미수락은 신호 발행 오류로 전파하지 않는다")
    void 종료된_Worker() {
        // given
        var relay = mock(EventOutboxRelayService.class);
        var policy = new OutboxRoutingConfig().outboxEventRoutingPolicy();
        var worker = new OutboxWorker(OutboxProcessingGroup.REALTIME, Runnable::run, relay, policy, 100);
        worker.close();

        // when / then
        listenerRunner(worker).run(context -> {
            assertThatCode(() -> context.publishEvent(new RealtimeOutboxCommittedEvent())).doesNotThrowAnyException();
            verifyNoInteractions(relay);
        });
    }

    @Test
    @DisplayName("예상하지 못한 실행 요청 예외도 신호 발행 오류로 전파하지 않는다")
    void 실행_요청_예외_격리() {
        // given
        var worker = mock(RequestOutboxRunUseCase.class);
        when(worker.requestRun()).thenThrow(new IllegalStateException("테스트 실행 실패"));

        // when / then
        listenerRunner(worker).run(context -> {
            assertThatCode(() -> context.publishEvent(new RealtimeOutboxCommittedEvent())).doesNotThrowAnyException();
            verify(worker).requestRun();
        });
    }

    private ApplicationContextRunner listenerRunner(RequestOutboxRunUseCase worker) {
        return new ApplicationContextRunner()
            .withUserConfiguration(RealtimeOutboxCommitListener.class)
            .withPropertyValues(ENABLED)
            .withBean("realtimeOutboxWorker", RequestOutboxRunUseCase.class, () -> worker);
    }

    private EventOutbox outbox() {
        return EventOutbox.record(new TestEvent(UUID.randomUUID(), Instant.now(), "chat.message.created"), "{}");
    }

    private record TestEvent(UUID eventId, Instant occurredAt, String eventType) implements DomainEvent {
    }
}
