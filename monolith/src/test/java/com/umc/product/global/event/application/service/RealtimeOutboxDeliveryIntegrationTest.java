package com.umc.product.global.event.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.umc.product.global.event.adapter.in.event.RealtimeOutboxCommitListener;
import com.umc.product.global.event.adapter.in.scheduler.EventOutboxPoller;
import com.umc.product.global.event.adapter.out.EventPayloadDeserializer;
import com.umc.product.global.event.adapter.out.EventPayloadSerializer;
import com.umc.product.global.event.adapter.out.OutboxCommitSignalPublisher;
import com.umc.product.global.event.adapter.out.OutboxDomainEventPublisher;
import com.umc.product.global.event.application.event.RealtimeOutboxCommittedEvent;
import com.umc.product.global.event.application.port.out.DomainEventPublisher;
import com.umc.product.global.event.application.port.out.LoadEventOutboxPort;
import com.umc.product.global.event.application.port.out.SaveEventOutboxPort;
import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.global.event.domain.EventOutbox;
import com.umc.product.global.event.domain.EventOutboxStatus;
import com.umc.product.global.event.domain.OutboxDispatchMode;
import com.umc.product.support.IntegrationTestSupport;

import io.micrometer.tracing.Tracer;

@Import(RealtimeOutboxDeliveryIntegrationTest.ProbeConfiguration.class)
@TestPropertySource(properties = {
    "app.event-outbox.relay-enabled=true",
    "app.event-outbox.workers.realtime.immediate-dispatch-enabled=true",
    "app.event-outbox.workers.realtime.concurrency=2",
    "app.event-outbox.workers.realtime.batch-size=2"
})
@DisplayName("실시간 Outbox 전달과 복구의 실제 DB 경계")
class RealtimeOutboxDeliveryIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private DomainEventPublisher publisher;
    @Autowired
    private ApplicationEventPublisher springEvents;
    @Autowired
    private SaveEventOutboxPort savePort;
    @Autowired
    private LoadEventOutboxPort loadPort;
    @Autowired
    private EventPayloadSerializer serializer;
    @Autowired
    private EventPayloadDeserializer deserializer;
    @Autowired
    private EventOutboxRelayService relay;
    @Autowired
    private OutboxEventRoutingPolicy policy;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private EventOutboxPoller poller;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private DeliveryProbe probe;
    @Autowired
    @Qualifier("realtimeOutboxExecutor") private ThreadPoolTaskExecutor realtimeExecutor;
    @Autowired
    @Qualifier("backgroundOutboxExecutor") private ThreadPoolTaskExecutor backgroundExecutor;

    @BeforeEach
    void 작업_준비() {
        // test 프로필의 SchedulingConfig 비활성화를 재사용하고 폴링은 직접 호출한다.
        awaitIdle();
    }

    @AfterEach
    void 비동기_실행_정리() {
        probe.releaseAll();
        awaitIdle();
        probe.states.clear();
    }

    @Test
    @DisplayName("커밋 전 폴링에는 보이지 않던 이벤트가 커밋 신호로 전달된다")
    void 커밋_이후_전달() {
        // given
        TestEvent event = TestEvent.create();
        DeliveryState state = probe.register(event, false, 0);

        // when
        transaction().executeWithoutResult(status -> {
            publisher.publish(event);
            poller.pollRealtime();
            awaitIdle();
            assertThat(state.attempts).hasValue(0);
        });

        // then
        awaitPublished(event);
        awaitIdle();
        assertThat(state.attempts).hasValue(1);
    }

    @Test
    @DisplayName("롤백한 이벤트는 DB에 남지 않고 신호와 폴링 어느 경로에서도 전달되지 않는다")
    void 롤백_미전달() {
        // given
        TestEvent event = TestEvent.create();
        DeliveryState state = probe.register(event, false, 0);

        // when
        transaction().executeWithoutResult(status -> {
            publisher.publish(event);
            status.setRollbackOnly();
        });
        poller.pollRealtime();
        awaitIdle();

        // then
        assertThat(rowCount(event)).isZero();
        assertThat(state.attempts).hasValue(0);
    }

    @Test
    @DisplayName("커밋 신호가 유실되어도 DB에 저장된 이벤트는 폴링으로 복구한다")
    void 신호_유실_복구() {
        // given
        TestEvent event = TestEvent.create();
        DeliveryState state = probe.register(event, false, 0);
        publishWithoutSignal(event);
        assertThat(snapshot(event).status()).isEqualTo("PENDING");
        assertThat(state.attempts).hasValue(0);

        // when
        poller.pollRealtime();

        // then
        awaitPublished(event);
        awaitIdle();
        assertThat(state.attempts).hasValue(1);
    }

    @Test
    @DisplayName("커밋 이후 executor가 요청을 거절해도 저장은 성공하고 폴링으로 복구한다")
    void 실행_거절_복구() {
        // given
        TestEvent event = TestEvent.create();
        DeliveryState state = probe.register(event, false, 0);
        try (var rejectingWorker = new OutboxWorker(OutboxProcessingGroup.REALTIME, task -> {
            throw new RejectedExecutionException("테스트 실행 거절");
        }, relay, policy, 2)) {
            var listener = new RealtimeOutboxCommitListener(rejectingWorker);

            // when
            assertThatCode(() -> publishWithSignal(event,
                signal -> listener.onCommitted((RealtimeOutboxCommittedEvent)signal))).doesNotThrowAnyException();

            // then
            assertThat(snapshot(event).status()).isEqualTo("PENDING");
            assertThat(state.attempts).hasValue(0);
            poller.pollRealtime();
            awaitPublished(event);
            awaitIdle();
            assertThat(state.attempts).hasValue(1);
        }
    }

    @Test
    @DisplayName("즉시 신호와 폴링이 경쟁해도 유효한 lease 동안 동일 이벤트를 중복 발행하지 않는다")
    void 즉시_처리와_폴링_경쟁() throws Exception {
        // given
        TestEvent event = TestEvent.create();
        DeliveryState state = probe.register(event, true, 0);
        publishWithoutSignal(event);
        CountDownLatch start = new CountDownLatch(1);
        var triggers = Executors.newFixedThreadPool(2);
        try {
            // when
            var immediate = triggers.submit(() -> {
                awaitLatch(start);
                springEvents.publishEvent(new RealtimeOutboxCommittedEvent());
            });
            var polling = triggers.submit(() -> {
                awaitLatch(start);
                poller.pollRealtime();
            });
            start.countDown();
            immediate.get(10, TimeUnit.SECONDS);
            polling.get(10, TimeUnit.SECONDS);
            assertThat(state.entered.await(10, TimeUnit.SECONDS)).isTrue();

            // then
            assertThat(snapshot(event).status()).isEqualTo("PROCESSING");
            assertThat(state.attempts).hasValue(1);
            state.release.countDown();
            awaitPublished(event);
            awaitIdle();
            assertThat(state.attempts).hasValue(1);
        } finally {
            start.countDown();
            state.release.countDown();
            triggers.shutdownNow();
            assertThat(triggers.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    @DisplayName("발행 실패 이벤트는 backoff 전에는 선점하지 않고 재시도 시각 이후 같은 ID로 복구한다")
    void 재시도_시각_준수() {
        // given
        TestEvent event = TestEvent.create();
        DeliveryState state = probe.register(event, false, 1);
        Instant before = Instant.now();
        publisher.publish(event);
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(snapshot(event).status()).isEqualTo("PENDING");
            assertThat(snapshot(event).attempts()).isEqualTo(1);
        });
        awaitIdle();
        Snapshot failed = snapshot(event);
        assertThat(failed.nextAttemptAt()).isAfter(before);
        List<EventOutbox> beforeDue = transaction().execute(status -> loadPort.listPublishable(
            policy.selectionFor(OutboxProcessingGroup.REALTIME), 10, failed.nextAttemptAt().minusMillis(1)
        ));
        assertThat(beforeDue).extracting(EventOutbox::getEventId).doesNotContain(event.eventId());
        assertThat(state.attempts).hasValue(1);

        // when
        makeDue(event);
        poller.pollRealtime();

        // then
        awaitPublished(event);
        awaitIdle();
        assertThat(state.attempts).hasValue(2);
        assertThat(rowCount(event)).isEqualTo(1);
        assertThat(snapshot(event).attempts()).isEqualTo(1);
    }

    @Test
    @DisplayName("처리 중 중단 상태는 lease 만료 전 제외하고 만료 후 복구하며 이전 소유자의 저장을 거부한다")
    void lease_복구와_이전_소유권_차단() {
        // given
        TestEvent event = TestEvent.create();
        DeliveryState state = probe.register(event, false, 0);
        publishWithoutSignal(event);
        EventOutbox stale = transaction().execute(status -> {
            List<EventOutbox> claimed = loadPort.listPublishable(
                policy.selectionFor(OutboxProcessingGroup.REALTIME), 1, Instant.now()
            );
            assertThat(claimed).hasSize(1);
            EventOutbox outbox = claimed.getFirst();
            outbox.markProcessing(Instant.now().plusSeconds(300));
            savePort.save(outbox);
            return outbox;
        });
        poller.pollRealtime();
        awaitIdle();
        assertThat(snapshot(event).status()).isEqualTo("PROCESSING");
        assertThat(state.attempts).hasValue(0);

        // when
        makeDue(event);
        poller.pollRealtime();
        awaitPublished(event);
        awaitIdle();

        // then
        assertThat(state.attempts).hasValue(1);
        assertThatThrownBy(() -> transaction().executeWithoutResult(status -> {
            stale.markPublished();
            savePort.save(stale);
        })).isInstanceOf(OptimisticLockingFailureException.class);
        assertThat(snapshot(event).status()).isEqualTo("PUBLISHED");
    }

    @Test
    @DisplayName("전달 후 완료 기록이 실패하면 같은 eventId로 재전달할 수 있고 최종 상태를 복구한다")
    void 전달_후_기록_실패_재전달() {
        // given
        TestEvent event = TestEvent.create();
        DeliveryState state = probe.register(event, false, 0);
        AtomicBoolean failCompletion = new AtomicBoolean(true);
        SaveEventOutboxPort failingSavePort = new SaveEventOutboxPort() {
            @Override
            public void save(EventOutbox outbox) {
                if (outbox.getStatus() == EventOutboxStatus.PUBLISHED && failCompletion.getAndSet(false)) {
                    throw new IllegalStateException("테스트 완료 기록 실패");
                }
                savePort.save(outbox);
            }

            @Override
            public void saveAll(Collection<EventOutbox> outboxes) {
                savePort.saveAll(outboxes);
            }
        };
        var failingRelay = new EventOutboxRelayService(loadPort, failingSavePort, deserializer, springEvents,
            transactionManager, Tracer.NOOP, 3);
        Queue<Runnable> tasks = new ArrayDeque<>();
        try (var worker = new OutboxWorker(OutboxProcessingGroup.REALTIME, tasks::add, failingRelay, policy, 2)) {
            var listener = new RealtimeOutboxCommitListener(worker);

            // when
            publishWithSignal(event, signal -> listener.onCommitted((RealtimeOutboxCommittedEvent)signal));
            tasks.remove().run();
            assertThat(state.attempts).hasValue(1);
            assertThat(snapshot(event).status()).isEqualTo("PENDING");
            assertThat(snapshot(event).attempts()).isEqualTo(1);
            makeDue(event);
            poller.pollRealtime();

            // then
            awaitPublished(event);
            awaitIdle();
            assertThat(state.attempts).hasValue(2);
            assertThat(rowCount(event)).isEqualTo(1);
        }
    }

    private void publishWithoutSignal(TestEvent event) {
        publishWithSignal(event, ignored -> {
        });
    }

    private void publishWithSignal(TestEvent event, ApplicationEventPublisher signals) {
        var signalPublisher = new OutboxCommitSignalPublisher(policy, signals);
        var writer = new OutboxDomainEventPublisher(savePort, serializer, Tracer.NOOP, signalPublisher);
        transaction().executeWithoutResult(status -> writer.publish(event));
    }

    private TransactionTemplate transaction() {
        return new TransactionTemplate(transactionManager);
    }

    private void awaitPublished(TestEvent event) {
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            assertThat(snapshot(event).status()).isEqualTo("PUBLISHED"));
    }

    private void awaitIdle() {
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(realtimeExecutor.getActiveCount()).isZero();
            assertThat(realtimeExecutor.getThreadPoolExecutor().getQueue()).isEmpty();
            assertThat(backgroundExecutor.getActiveCount()).isZero();
            assertThat(backgroundExecutor.getThreadPoolExecutor().getQueue()).isEmpty();
        });
    }

    private Snapshot snapshot(TestEvent event) {
        return jdbc.queryForObject("SELECT status, attempts, next_attempt_at FROM event_outbox WHERE event_id = ?",
            (row, index) -> new Snapshot(row.getString("status"), row.getInt("attempts"),
                row.getTimestamp("next_attempt_at").toInstant()), event.eventId());
    }

    private long rowCount(TestEvent event) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM event_outbox WHERE event_id = ?", Long.class, event.eventId());
    }

    private void makeDue(TestEvent event) {
        // 실제 대기 대신 DB의 재시도/lease 시각만 앞당기고 이전 소유자의 버전도 무효화한다.
        assertThat(jdbc.update("UPDATE event_outbox SET next_attempt_at = ?, version = version + 1 WHERE event_id = ?",
            Timestamp.from(Instant.now().minusSeconds(1)), event.eventId())).isEqualTo(1);
    }

    private static void awaitLatch(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("테스트 대기 시간 초과");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private record Snapshot(String status, int attempts, Instant nextAttemptAt) {
    }

    public record TestEvent(UUID eventId, Instant occurredAt) implements DomainEvent {
        static TestEvent create() {
            return new TestEvent(UUID.randomUUID(), Instant.now());
        }

        @Override
        public String eventType() {
            return "chat.test.delivery";
        }

        @Override
        public OutboxDispatchMode outboxDispatchMode() {
            return OutboxDispatchMode.NON_TRANSACTIONAL;
        }
    }

    @TestConfiguration
    static class ProbeConfiguration {
        @Bean
        DeliveryProbe deliveryProbe() {
            return new DeliveryProbe();
        }
    }

    static class DeliveryProbe {
        private final Map<UUID, DeliveryState> states = new ConcurrentHashMap<>();

        DeliveryState register(TestEvent event, boolean block, int failures) {
            DeliveryState state = new DeliveryState(block, failures);
            states.put(event.eventId(), state);
            return state;
        }

        @EventListener
        public void onEvent(TestEvent event) {
            DeliveryState state = states.get(event.eventId());
            if (state == null) {
                return;
            }
            state.attempts.incrementAndGet();
            state.entered.countDown();
            awaitLatch(state.release);
            if (state.failures.getAndUpdate(count -> Math.max(0, count - 1)) > 0) {
                throw new IllegalStateException("테스트 발행 실패");
            }
        }

        void releaseAll() {
            states.values().forEach(state -> state.release.countDown());
        }
    }

    private static class DeliveryState {
        private final AtomicInteger attempts = new AtomicInteger();
        private final AtomicInteger failures;
        private final CountDownLatch entered = new CountDownLatch(1);
        private final CountDownLatch release;

        DeliveryState(boolean block, int failures) {
            this.failures = new AtomicInteger(failures);
            release = new CountDownLatch(block ? 1 : 0);
        }
    }
}
