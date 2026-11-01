package com.umc.product.global.event.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.config.OutboxRoutingConfig;
import com.umc.product.global.event.adapter.out.EventPayloadDeserializer;
import com.umc.product.global.event.adapter.out.EventPayloadSerializer;
import com.umc.product.global.event.application.port.out.LoadEventOutboxPort;
import com.umc.product.global.event.application.port.out.OutboxEventSelection;
import com.umc.product.global.event.application.port.out.SaveEventOutboxPort;
import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.global.event.domain.EventOutbox;
import com.umc.product.global.event.domain.EventOutboxStatus;
import com.umc.product.global.event.domain.OutboxDispatchMode;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.test.simple.SimpleSpan;
import io.micrometer.tracing.test.simple.SimpleTracer;

@DisplayName("EventOutboxRelayService")
class EventOutboxRelayServiceTest {

    @Test
    @DisplayName("publishable outbox를 DomainEvent로 복원해 Spring event bus로 발행하고 published 처리한다")
    void relay_성공() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        EventPayloadSerializer serializer = new EventPayloadSerializer(objectMapper);
        TestEvent event = TestEvent.create("test.created", "hello");
        EventOutbox outbox = EventOutbox.record(event, serializer.serialize(event));
        FakeLoadEventOutboxPort loadPort = new FakeLoadEventOutboxPort(List.of(outbox));
        FakeSaveEventOutboxPort savePort = new FakeSaveEventOutboxPort();
        CapturingApplicationEventPublisher publisher = new CapturingApplicationEventPublisher();
        EventOutboxRelayService relayService = new EventOutboxRelayService(
            loadPort,
            savePort,
            new EventPayloadDeserializer(objectMapper),
            publisher,
            new LocalTransactionManager(),
            Tracer.NOOP,
            3
        );

        int claimed = relayService.relay(
            OutboxProcessingGroup.BACKGROUND,
            new OutboxRoutingConfig().outboxEventRoutingPolicy().selectionFor(OutboxProcessingGroup.BACKGROUND),
            100
        );

        assertThat(claimed).isEqualTo(1);
        assertThat(outbox.getStatus()).isEqualTo(EventOutboxStatus.PUBLISHED);
        assertThat(publisher.events).hasSize(1);
        assertThat(publisher.events.getFirst()).isInstanceOf(TestEvent.class);
        assertThat(((TestEvent) publisher.events.getFirst()).message()).isEqualTo("hello");
        assertThat(savePort.savedStatuses).contains(EventOutboxStatus.PROCESSING, EventOutboxStatus.PUBLISHED);
    }

    @Test
    @DisplayName("그룹별 실행도 기존 발행과 완료 기록을 유지한다")
    void 그룹별_relay_성공() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        EventPayloadSerializer serializer = new EventPayloadSerializer(objectMapper);
        TestEvent event = TestEvent.create("test.created", "hello");
        EventOutbox outbox = EventOutbox.record(event, serializer.serialize(event));
        FakeLoadEventOutboxPort loadPort = new FakeLoadEventOutboxPort(List.of(outbox));
        FakeSaveEventOutboxPort savePort = new FakeSaveEventOutboxPort();
        CapturingApplicationEventPublisher publisher = new CapturingApplicationEventPublisher();
        EventOutboxRelayService relayService = new EventOutboxRelayService(
            loadPort,
            savePort,
            new EventPayloadDeserializer(objectMapper),
            publisher,
            new LocalTransactionManager(),
            Tracer.NOOP,
            3
        );

        relayService.relay(
            OutboxProcessingGroup.BACKGROUND,
            new OutboxEventSelection("background"), 1
        );

        assertThat(outbox.getStatus()).isEqualTo(EventOutboxStatus.PUBLISHED);
        assertThat(publisher.events).hasSize(1);
        assertThat(publisher.events.getFirst()).isInstanceOf(TestEvent.class);
        assertThat(((TestEvent) publisher.events.getFirst()).message()).isEqualTo("hello");
        assertThat(savePort.savedStatuses).contains(EventOutboxStatus.PROCESSING, EventOutboxStatus.PUBLISHED);
    }

    @Test
    @DisplayName("이벤트 복원 또는 발행 실패 시 별도 상태 저장 트랜잭션에서 attempts를 증가시키고 pending으로 남긴다")
    void relay_실패_재시도() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        EventPayloadSerializer serializer = new EventPayloadSerializer(objectMapper);
        TestEvent event = TestEvent.create("test.created", "hello");
        EventOutbox outbox = EventOutbox.record(event, serializer.serialize(event));
        FakeLoadEventOutboxPort loadPort = new FakeLoadEventOutboxPort(List.of(outbox));
        FakeSaveEventOutboxPort savePort = new FakeSaveEventOutboxPort();
        ApplicationEventPublisher publisher = ignored -> {
            throw new IllegalStateException("publish failed");
        };
        EventOutboxRelayService relayService = new EventOutboxRelayService(
            loadPort,
            savePort,
            new EventPayloadDeserializer(objectMapper),
            publisher,
            new LocalTransactionManager(),
            Tracer.NOOP,
            3
        );

        int claimed = relayService.relay(
            OutboxProcessingGroup.BACKGROUND,
            new OutboxRoutingConfig().outboxEventRoutingPolicy().selectionFor(OutboxProcessingGroup.BACKGROUND),
            100
        );

        assertThat(claimed).isEqualTo(1);
        assertThat(outbox.getStatus()).isEqualTo(EventOutboxStatus.PENDING);
        assertThat(outbox.getAttempts()).isEqualTo(1);
        assertThat(outbox.getLastError()).contains("publish failed");
        assertThat(savePort.savedStatuses).contains(EventOutboxStatus.PROCESSING, EventOutboxStatus.PENDING);
    }

    @Test
    @DisplayName("그룹별 실행 실패도 기존 재시도 상태로 기록한다")
    void 그룹별_relay_실패_재시도() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        EventPayloadSerializer serializer = new EventPayloadSerializer(objectMapper);
        TestEvent event = TestEvent.create("test.created", "hello");
        EventOutbox outbox = EventOutbox.record(event, serializer.serialize(event));
        FakeLoadEventOutboxPort loadPort = new FakeLoadEventOutboxPort(List.of(outbox));
        FakeSaveEventOutboxPort savePort = new FakeSaveEventOutboxPort();
        ApplicationEventPublisher publisher = ignored -> {
            throw new IllegalStateException("publish failed");
        };
        EventOutboxRelayService relayService = new EventOutboxRelayService(
            loadPort,
            savePort,
            new EventPayloadDeserializer(objectMapper),
            publisher,
            new LocalTransactionManager(),
            Tracer.NOOP,
            3
        );

        relayService.relay(
            OutboxProcessingGroup.BACKGROUND,
            new OutboxEventSelection("background"), 1
        );

        assertThat(outbox.getStatus()).isEqualTo(EventOutboxStatus.PENDING);
        assertThat(outbox.getAttempts()).isEqualTo(1);
        assertThat(outbox.getLastError()).contains("publish failed");
        assertThat(savePort.savedStatuses).contains(EventOutboxStatus.PROCESSING, EventOutboxStatus.PENDING);
    }

    @Test
    @DisplayName("최대 재시도 횟수에 도달하면 failed 상태로 저장한다")
    void relay_최대_재시도_도달() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        EventPayloadSerializer serializer = new EventPayloadSerializer(objectMapper);
        TestEvent event = TestEvent.create("test.created", "hello");
        EventOutbox outbox = EventOutbox.record(event, serializer.serialize(event));
        outbox.recordFailure("previous", Instant.now(), 2);
        FakeLoadEventOutboxPort loadPort = new FakeLoadEventOutboxPort(List.of(outbox));
        FakeSaveEventOutboxPort savePort = new FakeSaveEventOutboxPort();
        ApplicationEventPublisher publisher = ignored -> {
            throw new IllegalStateException("publish failed");
        };
        EventOutboxRelayService relayService = new EventOutboxRelayService(
            loadPort,
            savePort,
            new EventPayloadDeserializer(objectMapper),
            publisher,
            new LocalTransactionManager(),
            Tracer.NOOP,
            2
        );

        relayService.relay(
            OutboxProcessingGroup.BACKGROUND,
            new OutboxRoutingConfig().outboxEventRoutingPolicy().selectionFor(OutboxProcessingGroup.BACKGROUND),
            100
        );

        assertThat(outbox.getStatus()).isEqualTo(EventOutboxStatus.FAILED);
        assertThat(outbox.getAttempts()).isEqualTo(2);
        assertThat(savePort.savedStatuses).contains(EventOutboxStatus.PROCESSING, EventOutboxStatus.FAILED);
    }

    @Test
    @DisplayName("저장된 traceparent가 있으면 relay 처리 span에 원 요청 trace로의 span link를 부착한다")
    void relay_span_link_부착() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        EventPayloadSerializer serializer = new EventPayloadSerializer(objectMapper);
        TestEvent event = TestEvent.create("test.created", "hello");
        String traceId = "0af7651916cd43dd8448eb211c80319c";
        String spanId = "b7ad6b7169203331";
        String traceparent = "00-" + traceId + "-" + spanId + "-01";
        EventOutbox outbox = EventOutbox.record(event, serializer.serialize(event), traceparent);
        FakeLoadEventOutboxPort loadPort = new FakeLoadEventOutboxPort(List.of(outbox));
        FakeSaveEventOutboxPort savePort = new FakeSaveEventOutboxPort();
        SimpleTracer tracer = new SimpleTracer();
        EventOutboxRelayService relayService = new EventOutboxRelayService(
            loadPort,
            savePort,
            new EventPayloadDeserializer(objectMapper),
            new CapturingApplicationEventPublisher(),
            new LocalTransactionManager(),
            tracer,
            3
        );

        relayService.relay(
            OutboxProcessingGroup.BACKGROUND,
            new OutboxRoutingConfig().outboxEventRoutingPolicy().selectionFor(OutboxProcessingGroup.BACKGROUND),
            100
        );

        SimpleSpan relaySpan = tracer.getSpans().stream()
            .filter(span -> "outbox.relay.publish".equals(span.getName()))
            .findFirst()
            .orElseThrow();
        assertThat(relaySpan.getLinks())
            .extracting(link -> link.getTraceContext().traceId())
            .containsExactly(traceId);
        assertThat(outbox.getStatus()).isEqualTo(EventOutboxStatus.PUBLISHED);
    }

    @Test
    @DisplayName("traceparent가 없으면 link 없이 relay 처리 span만 생성하고 정상 발행한다")
    void relay_traceparent_없음_link_미부착() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        EventPayloadSerializer serializer = new EventPayloadSerializer(objectMapper);
        TestEvent event = TestEvent.create("test.created", "hello");
        EventOutbox outbox = EventOutbox.record(event, serializer.serialize(event));
        FakeLoadEventOutboxPort loadPort = new FakeLoadEventOutboxPort(List.of(outbox));
        FakeSaveEventOutboxPort savePort = new FakeSaveEventOutboxPort();
        SimpleTracer tracer = new SimpleTracer();
        EventOutboxRelayService relayService = new EventOutboxRelayService(
            loadPort,
            savePort,
            new EventPayloadDeserializer(objectMapper),
            new CapturingApplicationEventPublisher(),
            new LocalTransactionManager(),
            tracer,
            3
        );

        relayService.relay(
            OutboxProcessingGroup.BACKGROUND,
            new OutboxRoutingConfig().outboxEventRoutingPolicy().selectionFor(OutboxProcessingGroup.BACKGROUND),
            100
        );

        SimpleSpan relaySpan = tracer.getSpans().stream()
            .filter(span -> "outbox.relay.publish".equals(span.getName()))
            .findFirst()
            .orElseThrow();
        assertThat(relaySpan.getLinks()).isEmpty();
        assertThat(outbox.getStatus()).isEqualTo(EventOutboxStatus.PUBLISHED);
    }

    @Test
    @DisplayName("non-transactional listener 성공 후 published 저장이 실패하면 재시도 대상(PENDING)으로 남긴다")
    void relay_published_저장_실패_재시도() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        EventPayloadSerializer serializer = new EventPayloadSerializer(objectMapper);
        NonTransactionalTestEvent event = NonTransactionalTestEvent.create("test.external.created", "hello");
        EventOutbox outbox = EventOutbox.record(event, serializer.serialize(event));
        FakeLoadEventOutboxPort loadPort = new FakeLoadEventOutboxPort(List.of(outbox));
        FailOnPublishedSaveEventOutboxPort savePort = new FailOnPublishedSaveEventOutboxPort();
        CapturingApplicationEventPublisher publisher = new CapturingApplicationEventPublisher();
        EventOutboxRelayService relayService = new EventOutboxRelayService(
            loadPort,
            savePort,
            new EventPayloadDeserializer(objectMapper),
            publisher,
            new LocalTransactionManager(),
            Tracer.NOOP,
            3
        );

        relayService.relay(
            OutboxProcessingGroup.BACKGROUND,
            new OutboxRoutingConfig().outboxEventRoutingPolicy().selectionFor(OutboxProcessingGroup.BACKGROUND),
            100
        );

        assertThat(outbox.getStatus()).isEqualTo(EventOutboxStatus.PENDING);
        assertThat(outbox.getAttempts()).isEqualTo(1);
        assertThat(publisher.events).hasSize(1);
        assertThat(savePort.savedStatuses).contains(EventOutboxStatus.PROCESSING, EventOutboxStatus.PENDING);
    }

    @Test
    @DisplayName("non-transactional 이벤트는 listener 실행 후 별도 트랜잭션으로 published 처리한다")
    void relay_non_transactional_dispatch() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        EventPayloadSerializer serializer = new EventPayloadSerializer(objectMapper);
        NonTransactionalTestEvent event = NonTransactionalTestEvent.create("test.external.created", "hello");
        EventOutbox outbox = EventOutbox.record(event, serializer.serialize(event));
        FakeSaveEventOutboxPort savePort = new FakeSaveEventOutboxPort();
        AtomicBoolean transactionActiveDuringPublish = new AtomicBoolean(true);
        ApplicationEventPublisher publisher = ignored ->
            transactionActiveDuringPublish.set(TransactionSynchronizationManager.isActualTransactionActive());
        EventOutboxRelayService relayService = new EventOutboxRelayService(
            new FakeLoadEventOutboxPort(List.of(outbox)),
            savePort,
            new EventPayloadDeserializer(objectMapper),
            publisher,
            new LocalTransactionManager(),
            Tracer.NOOP,
            3
        );

        relayService.relay(
            OutboxProcessingGroup.BACKGROUND,
            new OutboxRoutingConfig().outboxEventRoutingPolicy().selectionFor(OutboxProcessingGroup.BACKGROUND),
            100
        );

        assertThat(transactionActiveDuringPublish).isFalse();
        assertThat(outbox.getStatus()).isEqualTo(EventOutboxStatus.PUBLISHED);
        assertThat(savePort.savedStatuses).contains(EventOutboxStatus.PROCESSING, EventOutboxStatus.PUBLISHED);
    }

    @Test
    @DisplayName("non-transactional listener 예외는 attempts를 증가시키고 pending 재시도로 연결한다")
    void relay_non_transactional_listener_failure() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        EventPayloadSerializer serializer = new EventPayloadSerializer(objectMapper);
        NonTransactionalTestEvent event = NonTransactionalTestEvent.create("test.external.created", "hello");
        EventOutbox outbox = EventOutbox.record(event, serializer.serialize(event));
        FakeSaveEventOutboxPort savePort = new FakeSaveEventOutboxPort();
        ApplicationEventPublisher publisher = ignored -> {
            throw new IllegalStateException("external call failed");
        };
        EventOutboxRelayService relayService = new EventOutboxRelayService(
            new FakeLoadEventOutboxPort(List.of(outbox)),
            savePort,
            new EventPayloadDeserializer(objectMapper),
            publisher,
            new LocalTransactionManager(),
            Tracer.NOOP,
            3
        );

        relayService.relay(
            OutboxProcessingGroup.BACKGROUND,
            new OutboxRoutingConfig().outboxEventRoutingPolicy().selectionFor(OutboxProcessingGroup.BACKGROUND),
            100
        );

        assertThat(outbox.getStatus()).isEqualTo(EventOutboxStatus.PENDING);
        assertThat(outbox.getAttempts()).isEqualTo(1);
        assertThat(outbox.getLastError()).contains("external call failed");
        assertThat(savePort.savedStatuses).contains(EventOutboxStatus.PROCESSING, EventOutboxStatus.PENDING);
    }

    @Test
    @DisplayName("lease 소유권을 잃은 worker는 published 상태 저장 실패를 재시도로 덮어쓰지 않는다")
    void relay_optimistic_lock_failure_does_not_overwrite_new_owner() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        EventPayloadSerializer serializer = new EventPayloadSerializer(objectMapper);
        NonTransactionalTestEvent event = NonTransactionalTestEvent.create("test.external.created", "hello");
        EventOutbox outbox = EventOutbox.record(event, serializer.serialize(event));
        LoseLeaseOnPublishedSaveEventOutboxPort savePort = new LoseLeaseOnPublishedSaveEventOutboxPort();
        CapturingApplicationEventPublisher publisher = new CapturingApplicationEventPublisher();
        EventOutboxRelayService relayService = new EventOutboxRelayService(
            new FakeLoadEventOutboxPort(List.of(outbox)),
            savePort,
            new EventPayloadDeserializer(objectMapper),
            publisher,
            new LocalTransactionManager(),
            Tracer.NOOP,
            3
        );

        relayService.relay(
            OutboxProcessingGroup.BACKGROUND,
            new OutboxRoutingConfig().outboxEventRoutingPolicy().selectionFor(OutboxProcessingGroup.BACKGROUND),
            100
        );

        assertThat(publisher.events).hasSize(1);
        assertThat(savePort.savedStatuses).containsExactly(EventOutboxStatus.PROCESSING);
    }

    private static class FailOnPublishedSaveEventOutboxPort implements SaveEventOutboxPort {

        private final List<EventOutboxStatus> savedStatuses = new ArrayList<>();

        @Override
        public void save(EventOutbox eventOutbox) {
            // published 상태 저장(= markPublished 영속화)만 실패시켜, 발행 단위 트랜잭션 실패를 재현한다.
            if (eventOutbox.getStatus() == EventOutboxStatus.PUBLISHED) {
                throw new IllegalStateException("published 저장 실패");
            }
            savedStatuses.add(eventOutbox.getStatus());
        }

        @Override
        public void saveAll(Collection<EventOutbox> eventOutboxes) {
            eventOutboxes.forEach(eventOutbox -> savedStatuses.add(eventOutbox.getStatus()));
        }
    }

    private static class LoseLeaseOnPublishedSaveEventOutboxPort implements SaveEventOutboxPort {

        private final List<EventOutboxStatus> savedStatuses = new ArrayList<>();

        @Override
        public void save(EventOutbox eventOutbox) {
            if (eventOutbox.getStatus() == EventOutboxStatus.PUBLISHED) {
                throw new OptimisticLockingFailureException("outbox lease 소유권 변경");
            }
            savedStatuses.add(eventOutbox.getStatus());
        }

        @Override
        public void saveAll(Collection<EventOutbox> eventOutboxes) {
            eventOutboxes.forEach(eventOutbox -> savedStatuses.add(eventOutbox.getStatus()));
        }
    }

    private static class FakeLoadEventOutboxPort implements LoadEventOutboxPort {

        private final List<EventOutbox> outboxes;

        private FakeLoadEventOutboxPort(List<EventOutbox> outboxes) {
            this.outboxes = outboxes;
        }

        @Override
        public List<EventOutbox> listPublishable(
            OutboxEventSelection selection,
            int limit, Instant now
        ) {
            var policy = new com.umc.product.global.config.OutboxRoutingConfig().outboxEventRoutingPolicy();
            return outboxes.stream().filter(outbox ->
                policy.resolveProcessingGroup(outbox.getEventType()).equals(selection.processingGroup()))
                .limit(limit).toList();
        }
    }

    @Test
    @DisplayName("선점 가능한 이벤트가 없으면 0건을 반환하고 발행하지 않는다")
    void 빈_배치_반환() {
        // given
        var mapper = new ObjectMapper().findAndRegisterModules();
        var publisher = new CapturingApplicationEventPublisher();
        var relay = new EventOutboxRelayService(
            new FakeLoadEventOutboxPort(List.of()), new FakeSaveEventOutboxPort(),
            new EventPayloadDeserializer(mapper), publisher, new LocalTransactionManager(), Tracer.NOOP, 3
        );

        // when
        int claimed = relay.relay(OutboxProcessingGroup.REALTIME, new OutboxEventSelection("realtime"), 10);

        // then
        assertThat(claimed).isZero();
        assertThat(publisher.events).isEmpty();
    }

    @Test
    @DisplayName("실제 Relay가 반환한 선점 수로 한 신호의 실시간 적체를 모두 발행한다")
    void 실제_Relay_연속_배치() {
        // given
        var mapper = new ObjectMapper().findAndRegisterModules();
        var serializer = new EventPayloadSerializer(mapper);
        List<EventOutbox> outboxes = new ArrayList<>();
        for (int index = 0; index < 23; index++) {
            TestEvent event = TestEvent.create("chat.message.created", "hello");
            outboxes.add(EventOutbox.record(event, serializer.serialize(event)));
        }
        AtomicInteger queries = new AtomicInteger();
        LoadEventOutboxPort loadPort = (selection, limit, now) -> {
            queries.incrementAndGet();
            return outboxes.stream().filter(outbox -> outbox.getStatus() == EventOutboxStatus.PENDING)
                .limit(limit).toList();
        };
        var publisher = new CapturingApplicationEventPublisher();
        var relay = new EventOutboxRelayService(
            loadPort, new FakeSaveEventOutboxPort(), new EventPayloadDeserializer(mapper), publisher,
            new LocalTransactionManager(), Tracer.NOOP, 3
        );
        Queue<Runnable> tasks = new ArrayDeque<>();
        var worker = new OutboxWorker(
            OutboxProcessingGroup.REALTIME, tasks::add, relay, new OutboxRoutingConfig().outboxEventRoutingPolicy(), 10
        );

        // when
        worker.requestRun();
        tasks.remove().run();

        // then
        assertThat(publisher.events).hasSize(23);
        assertThat(outboxes).allMatch(outbox -> outbox.getStatus() == EventOutboxStatus.PUBLISHED);
        assertThat(queries).hasValue(3);
        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("실패 이벤트가 재시도 대기 상태로 남아도 빈 배치 조회 후 반복을 멈춘다")
    void 재시도_대기_후_반복_종료() {
        // given
        var mapper = new ObjectMapper().findAndRegisterModules();
        TestEvent event = TestEvent.create("chat.message.created", "hello");
        EventOutbox outbox = EventOutbox.record(event, new EventPayloadSerializer(mapper).serialize(event));
        Instant eligibleAt = Instant.now();
        AtomicInteger queries = new AtomicInteger();
        LoadEventOutboxPort loadPort = (selection, limit, now) -> {
            queries.incrementAndGet();
            // 동일한 조회 시점에서 backoff 이후 이벤트가 제외되는 상황을 재현한다.
            return outbox.getStatus() == EventOutboxStatus.PENDING && !outbox.getNextAttemptAt().isAfter(eligibleAt)
                ? List.of(outbox) : List.of();
        };
        AtomicInteger attempts = new AtomicInteger();
        var relay = new EventOutboxRelayService(
            loadPort, new FakeSaveEventOutboxPort(), new EventPayloadDeserializer(mapper), ignored -> {
                attempts.incrementAndGet();
                throw new IllegalStateException("발행 실패");
            }, new LocalTransactionManager(), Tracer.NOOP, 3
        );
        Queue<Runnable> tasks = new ArrayDeque<>();
        var worker = new OutboxWorker(
            OutboxProcessingGroup.REALTIME, tasks::add, relay, new OutboxRoutingConfig().outboxEventRoutingPolicy(), 1
        );

        // when
        worker.requestRun();
        tasks.remove().run();

        // then
        assertThat(queries).hasValue(2);
        assertThat(attempts).hasValue(1);
        assertThat(outbox.getStatus()).isEqualTo(EventOutboxStatus.PENDING);
        assertThat(outbox.getNextAttemptAt()).isAfter(eligibleAt);
        assertThat(tasks).isEmpty();
    }

    private static class FakeSaveEventOutboxPort implements SaveEventOutboxPort {

        private final List<EventOutboxStatus> savedStatuses = new ArrayList<>();

        @Override
        public void save(EventOutbox eventOutbox) {
            savedStatuses.add(eventOutbox.getStatus());
        }

        @Override
        public void saveAll(Collection<EventOutbox> eventOutboxes) {
            eventOutboxes.forEach(eventOutbox -> savedStatuses.add(eventOutbox.getStatus()));
        }
    }

    private static class CapturingApplicationEventPublisher implements ApplicationEventPublisher {

        private final List<Object> events = new ArrayList<>();

        @Override
        public void publishEvent(Object event) {
            events.add(event);
        }
    }

    public record TestEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        String message
    ) implements DomainEvent {

        static TestEvent create(String eventType, String message) {
            return new TestEvent(UUID.randomUUID(), Instant.now(), eventType, message);
        }
    }

    public record NonTransactionalTestEvent(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        String message
    ) implements DomainEvent {

        static NonTransactionalTestEvent create(String eventType, String message) {
            return new NonTransactionalTestEvent(UUID.randomUUID(), Instant.now(), eventType, message);
        }

        @Override
        public OutboxDispatchMode outboxDispatchMode() {
            return OutboxDispatchMode.NON_TRANSACTIONAL;
        }
    }

    private static class LocalTransactionManager extends AbstractPlatformTransactionManager {

        @Override
        protected Object doGetTransaction() throws TransactionException {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        }

        @Override
        protected boolean isExistingTransaction(Object transaction) throws TransactionException {
            return false;
        }
    }
}
