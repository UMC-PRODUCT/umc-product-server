package com.umc.product.global.event.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.umc.product.global.config.OutboxRoutingConfig;
import com.umc.product.global.event.application.service.OutboxEventRoutingPolicy;
import com.umc.product.global.event.application.service.OutboxEventRoutingPolicy.Rule;
import com.umc.product.global.event.application.service.OutboxProcessingGroup;
import com.umc.product.global.event.domain.DomainEvent;
import com.umc.product.global.event.domain.EventOutbox;
import com.umc.product.support.PersistenceAdapterTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;

@Import(EventOutboxQueryRepository.class)
@PersistenceAdapterTest
@DisplayName("EventOutboxJpaRepository")
class EventOutboxJpaRepositoryTest {

    @Autowired
    private EventOutboxQueryRepository queryRepository;

    private static final Instant NOW = Instant.parse("2026-07-13T00:00:00Z");

    @Autowired
    private EventOutboxJpaRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("발행 가능한 이벤트를 다음 시도 시각 순서로 조회한다")
    void findPublishableForUpdateOrdersByNextAttemptAt() {
        UUID laterEventId = insertOutbox("PENDING", NOW.minusSeconds(1));
        UUID earlierEventId = insertOutbox("PENDING", NOW.minusSeconds(10));
        insertOutbox("PUBLISHED", NOW.minusSeconds(20));
        entityManager.flush();
        entityManager.clear();

        List<EventOutbox> result = queryRepository.findPublishableForUpdate(
            new OutboxRoutingConfig().outboxEventRoutingPolicy().selectionFor(OutboxProcessingGroup.BACKGROUND),
            100, NOW
        );

        assertThat(result)
            .extracting(EventOutbox::getEventId)
            .containsExactly(earlierEventId, laterEventId);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("lease를 다시 획득한 뒤에는 이전 worker가 outbox 상태를 덮어쓸 수 없다")
    void staleWorkerCannotOverwriteReclaimedOutbox() {
        EventOutbox recorded = EventOutbox.record(TestEvent.create(), "{}");
        Long outboxId = persist(recorded);
        EventOutbox staleWorkerCopy = findDetached(outboxId);

        EntityManager reclaimingEntityManager = entityManagerFactory.createEntityManager();
        reclaimingEntityManager.getTransaction().begin();
        EventOutbox reclaimed = reclaimingEntityManager.find(EventOutbox.class, outboxId);
        reclaimed.markProcessing(Instant.now().plusSeconds(300));
        reclaimingEntityManager.getTransaction().commit();
        reclaimingEntityManager.close();

        EntityManager staleEntityManager = entityManagerFactory.createEntityManager();
        staleEntityManager.getTransaction().begin();
        staleWorkerCopy.markPublished();

        assertThatThrownBy(() -> {
            staleEntityManager.merge(staleWorkerCopy);
            staleEntityManager.flush();
        }).isInstanceOf(OptimisticLockException.class);

        staleEntityManager.getTransaction().rollback();
        staleEntityManager.close();
        delete(outboxId);
    }

    @Test
    @DisplayName("혼합된 이벤트에서 각 그룹은 자기 대상만 선점하고 만료되지 않은 lease는 제외한다")
    void 그룹별_선점_격리() {
        // Given
        UUID chat = insertOutbox("chat.message.created", "PENDING", NOW.minusSeconds(10));
        UUID lifecycle = insertOutbox("community.thread.member.kicked", "PROCESSING", NOW.minusSeconds(5));
        UUID alarm = insertOutbox("community.thread.message.created", "PENDING", NOW.minusSeconds(20));
        UUID fcm = insertOutbox("notification.fcm.batch.requested", "PENDING", NOW.minusSeconds(15));
        insertOutbox("chat.message.created", "PROCESSING", NOW.plusSeconds(300));
        insertOutbox("chat.message.created", "PUBLISHED", NOW.minusSeconds(30));
        entityManager.flush();
        entityManager.clear();
        var policy = new OutboxRoutingConfig().outboxEventRoutingPolicy();

        // When & Then
        assertThat(queryRepository.findPublishableForUpdate(
            policy.selectionFor(OutboxProcessingGroup.REALTIME), 100, NOW
        )).extracting(EventOutbox::getEventId).containsExactly(chat, lifecycle);
        assertThat(queryRepository.findPublishableForUpdate(
            policy.selectionFor(OutboxProcessingGroup.BACKGROUND), 100, NOW
        )).extracting(EventOutbox::getEventId).containsExactly(alarm, fcm);
        assertThat(queryRepository.findPublishableForUpdate(
            policy.selectionFor(OutboxProcessingGroup.REALTIME), 1, NOW
        )).extracting(EventOutbox::getEventId).containsExactly(chat);
    }

    @Test
    @DisplayName("명시적 Chat 예외와 추가 그룹도 선점 조건에 반영한다")
    void 명시적_예외와_추가_그룹() {
        var analytics = new OutboxProcessingGroup("analytics");
        var policy = new OutboxEventRoutingPolicy(
            Set.of(OutboxProcessingGroup.REALTIME, OutboxProcessingGroup.BACKGROUND, analytics),
            OutboxProcessingGroup.BACKGROUND, List.of(
                new Rule(
                    "chat.statistics.created", analytics)
            ), "chat.", OutboxProcessingGroup.REALTIME
        );
        UUID statistics = insertOutbox("chat.statistics.created", "PENDING", NOW.minusSeconds(10));
        UUID chat = insertOutbox("chat.message.created", "PENDING", NOW.minusSeconds(5));
        UUID unknown = insertOutbox("unknown.created", "PENDING", NOW.minusSeconds(1));

        assertThat(queryRepository.findPublishableForUpdate(policy.selectionFor(analytics), 100, NOW))
            .extracting(EventOutbox::getEventId).containsExactly(statistics);
        assertThat(queryRepository.findPublishableForUpdate(
            policy.selectionFor(OutboxProcessingGroup.REALTIME), 100, NOW
        )).extracting(EventOutbox::getEventId).containsExactly(chat);
        assertThat(queryRepository.findPublishableForUpdate(
            policy.selectionFor(OutboxProcessingGroup.BACKGROUND), 100, NOW
        )).extracting(EventOutbox::getEventId).containsExactly(unknown);
    }

    @Test
    @DisplayName("적체 집계는 재시도 대기와 만료 전 lease도 포함하고 완료 이벤트는 제외한다")
    void 그룹별_적체_집계() {
        insertOutbox("chat.message.created", "PENDING", NOW.plusSeconds(30));
        insertOutbox("chat.message.updated", "PROCESSING", NOW.plusSeconds(300));
        insertOutbox("chat.message.deleted", "PUBLISHED", NOW.minusSeconds(1));
        insertOutbox("notification.fcm.batch.requested", "PENDING", NOW);
        var policy = new OutboxRoutingConfig().outboxEventRoutingPolicy();

        var realtime = queryRepository.getBacklog(policy.selectionFor(OutboxProcessingGroup.REALTIME));
        var background = queryRepository.getBacklog(policy.selectionFor(OutboxProcessingGroup.BACKGROUND));

        assertThat(realtime.pending()).isEqualTo(1);
        assertThat(realtime.processing()).isEqualTo(1);
        assertThat(realtime.oldestAgeSeconds()).isGreaterThanOrEqualTo(0);
        assertThat(background.pending()).isEqualTo(1);
        assertThat(background.processing()).isZero();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("동일 그룹의 두 실행은 잠긴 row를 건너뛰고 서로 다른 이벤트를 선점한다")
    void 병렬_선점_중복_방지() throws Exception {
        Long first = persist(EventOutbox.record(TestEvent.create(), "{}"));
        Long second = persist(EventOutbox.record(TestEvent.create(), "{}"));
        CountDownLatch claimed = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        var selection = new com.umc.product.global.event.application.port.out.OutboxEventSelection(
            Set.of("test.event"), Set.of("test.event"), "chat.",
            com.umc.product.global.event.application.port.out.OutboxEventSelection.PrefixMatch.NONE
        );
        var executor = Executors.newFixedThreadPool(2);
        java.util.concurrent.Callable<List<Long>> claim = () -> new TransactionTemplate(transactionManager)
            .execute(status -> {
                var events = queryRepository.findPublishableForUpdate(selection, 1, Instant.now());
                claimed.countDown();
                try {
                    if (!release.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("테스트 대기 시간 초과");
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                }
                events.forEach(event -> event.markProcessing(Instant.now().plusSeconds(300)));
                entityManager.flush();
                return events.stream().map(EventOutbox::getId).toList();
            });
        try {
            var resultA = executor.submit(claim);
            var resultB = executor.submit(claim);
            assertThat(claimed.await(5, TimeUnit.SECONDS)).isTrue();
            release.countDown();
            var idsA = resultA.get(5, TimeUnit.SECONDS);
            var idsB = resultB.get(5, TimeUnit.SECONDS);
            assertThat(idsA).hasSize(1);
            assertThat(idsB).hasSize(1).doesNotContainAnyElementsOf(idsA);
            assertThat(java.util.stream.Stream.concat(idsA.stream(), idsB.stream()).toList())
                .containsExactlyInAnyOrder(first, second);
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
            delete(first);
            delete(second);
        }
    }

    private Long persist(EventOutbox outbox) {
        EntityManager setupEntityManager = entityManagerFactory.createEntityManager();
        setupEntityManager.getTransaction().begin();
        setupEntityManager.persist(outbox);
        setupEntityManager.getTransaction().commit();
        Long outboxId = outbox.getId();
        setupEntityManager.close();
        return outboxId;
    }

    private EventOutbox findDetached(Long outboxId) {
        EntityManager workerEntityManager = entityManagerFactory.createEntityManager();
        EventOutbox outbox = workerEntityManager.find(EventOutbox.class, outboxId);
        workerEntityManager.close();
        return outbox;
    }

    private void delete(Long outboxId) {
        EntityManager cleanupEntityManager = entityManagerFactory.createEntityManager();
        cleanupEntityManager.getTransaction().begin();
        EventOutbox outbox = cleanupEntityManager.find(EventOutbox.class, outboxId);
        cleanupEntityManager.remove(outbox);
        cleanupEntityManager.getTransaction().commit();
        cleanupEntityManager.close();
    }

    private UUID insertOutbox(String status, Instant nextAttemptAt) {
        return insertOutbox("test.event", status, nextAttemptAt);
    }

    private UUID insertOutbox(String eventType, String status, Instant nextAttemptAt) {
        UUID eventId = UUID.randomUUID();
        entityManager.createNativeQuery("""
                INSERT INTO event_outbox (
                    event_id, event_type, event_class, payload, status, attempts,
                    next_attempt_at, created_at, updated_at
                ) VALUES (
                    :eventId, :eventType, 'test.Event', CAST('{}' AS jsonb), :status, 0,
                    :nextAttemptAt, :createdAt, :updatedAt
                )
                """)
            .setParameter("eventId", eventId)
            .setParameter("eventType", eventType)
            .setParameter("status", status)
            .setParameter("nextAttemptAt", nextAttemptAt)
            .setParameter("createdAt", NOW)
            .setParameter("updatedAt", NOW)
            .executeUpdate();
        return eventId;
    }

    private record TestEvent(UUID eventId, Instant occurredAt) implements DomainEvent {

        private static TestEvent create() {
            return new TestEvent(UUID.randomUUID(), Instant.now());
        }

        @Override
        public String eventType() {
            return "test.event";
        }
    }
}
