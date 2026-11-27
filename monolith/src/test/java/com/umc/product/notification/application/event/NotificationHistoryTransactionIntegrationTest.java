package com.umc.product.notification.application.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.umc.product.global.event.adapter.out.EventPayloadDeserializer;
import com.umc.product.global.event.application.port.out.DomainEventPublisher;
import com.umc.product.global.event.application.port.out.LoadEventOutboxPort;
import com.umc.product.global.event.application.port.out.SaveEventOutboxPort;
import com.umc.product.global.event.application.service.EventOutboxRelayMetrics;
import com.umc.product.global.event.application.service.EventOutboxRelayService;
import com.umc.product.global.event.application.service.OutboxEventRoutingPolicy;
import com.umc.product.global.event.application.service.OutboxProcessingGroup;
import com.umc.product.notification.application.port.in.RequestFcmNotificationUseCase;
import com.umc.product.notification.application.port.in.dto.RequestFcmNotificationCommand;
import com.umc.product.notification.application.port.out.LoadFcmPort;
import com.umc.product.notification.application.port.out.SaveNotificationHistoryPort;
import com.umc.product.notification.domain.FcmToken;
import com.umc.product.notification.domain.NotificationHistory;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.FcmTokenFixture;
import com.umc.product.support.fixture.MemberFixture;

import io.micrometer.tracing.Tracer;

@TestPropertySource(properties = "app.fcm.enabled=true")
class NotificationHistoryTransactionIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MemberFixture members;
    @Autowired
    private FcmTokenFixture tokens;
    @Autowired
    private SaveNotificationHistoryPort historyPort;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private RequestFcmNotificationUseCase requestNotification;
    @Autowired
    private LoadEventOutboxPort loadOutbox;
    @Autowired
    private SaveEventOutboxPort saveOutbox;
    @Autowired
    private EventPayloadDeserializer deserializer;
    @Autowired
    private ApplicationEventPublisher springEvents;
    @Autowired
    private EventOutboxRelayService relay;
    @Autowired
    private OutboxEventRoutingPolicy routingPolicy;
    @Autowired
    private ObjectProvider<Tracer> tracerProvider;
    @Autowired
    private EventOutboxRelayMetrics relayMetrics;
    @Autowired
    private DomainEventPublisher outboxPublisher;
    @Autowired
    private LoadFcmPort loadFcmPort;

    @Test
    @DisplayName("두 트랜잭션이 같은 요청과 회원의 이력을 저장하면 한 건만 커밋된다")
    void 동시_저장_중복_방지() throws Exception {
        // given
        Long memberId = members.일반("동시저장회원").getId();
        var history = new NotificationHistory(UUID.randomUUID(),
            "제목", "본문", null, null, Instant.now());
        var firstInserted = new CountDownLatch(1);
        var allowFirstCommit = new CountDownLatch(1);
        var secondBackendPid = new CompletableFuture<Integer>();
        var transaction = new TransactionTemplate(transactionManager);
        transaction.setTimeout(30);

        // when
        try (var workers = Executors.newFixedThreadPool(2)) {
            var first = workers.submit(() -> transaction.executeWithoutResult(status -> {
                historyPort.saveIfAbsent(history, List.of(memberId));
                firstInserted.countDown();
                awaitLatch(allowFirstCommit);
            }));
            var second = workers.submit(() -> {
                awaitLatch(firstInserted);
                transaction.executeWithoutResult(status -> {
                    secondBackendPid.complete(jdbc.queryForObject("SELECT pg_backend_pid()", Integer.class));
                    historyPort.saveIfAbsent(history, List.of(memberId));
                });
            });

            try {
                int pid = secondBackendPid.get(10, TimeUnit.SECONDS);
                await().atMost(Duration.ofSeconds(10)).until(() -> Boolean.TRUE.equals(jdbc.queryForObject(
                    "SELECT cardinality(pg_blocking_pids(?)) > 0", Boolean.class, pid
                )));
            } finally {
                allowFirstCommit.countDown();
            }
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        }

        // then
        assertThat(historyCount(history.requestId())).isEqualTo(1);
    }

    @Test
    @DisplayName("FCM 배치 저장 단계가 실패하면 이력도 롤백되고 재시도에서 함께 커밋된다")
    void 알림_이력과_FCM_배치_롤백_후_재시도() {
        // given
        Long memberId = members.일반("재시도회원").getId();
        tokens.FCM_토큰(memberId, "inbox-retry-installation", "inbox-retry-token");
        String imageUrl = "https://example.com/image.png";
        UUID requestId = requestNotification.request(RequestFcmNotificationCommand.builder()
            .memberIds(List.of(memberId))
            .title("공지")
            .body("본문")
            .imageUrl(imageUrl)
            .build()).requestId();
        var group = OutboxProcessingGroup.BACKGROUND;
        var selection = routingPolicy.selectionFor(group);
        ApplicationEventPublisher failAfterBatchInsert = event -> {
            springEvents.publishEvent(event);
            entityManager.flush();
            assertThat(historyCount(requestId)).isEqualTo(1);
            assertThat(batchCount(requestId)).isEqualTo(1);
            throw new IllegalStateException("FCM 배치 저장 단계 실패");
        };
        var failingRelay = new EventOutboxRelayService(loadOutbox, saveOutbox, deserializer, failAfterBatchInsert,
            transactionManager, tracerProvider, relayMetrics, 3);

        // when
        assertThat(failingRelay.relay(group, selection, 1)).isEqualTo(1);

        // then
        assertThat(historyCount(requestId)).isZero();
        assertThat(batchCount(requestId)).isZero();
        assertThat(requestStatus(requestId)).isEqualTo("PENDING");
        assertThat(jdbc.update("""
            UPDATE event_outbox SET next_attempt_at = TIMESTAMPTZ '2000-01-01 00:00:00+00'
            WHERE event_type = 'notification.fcm.requested' AND payload::jsonb ->> 'requestId' = ?
            """, requestId.toString())).isEqualTo(1);

        // when / then
        assertThat(relay.relay(group, selection, 1)).isEqualTo(1);
        assertThat(requestStatus(requestId)).isEqualTo("PUBLISHED");
        assertThat(historyCount(requestId)).isEqualTo(1);
        assertThat(batchCount(requestId)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
            "SELECT image_url FROM notification_history WHERE request_id = ?", String.class, requestId
        )).isEqualTo(imageUrl);
    }

    @Test
    @DisplayName("구버전 소비자가 신버전 요청을 먼저 처리하면 푸시 배치는 남고 알림 이력은 누락된다")
    void 혼합_배포에서_구버전이_요청을_선점한다() throws Exception {
        // given
        Long memberId = members.일반("혼합배포회원").getId();
        tokens.FCM_토큰(memberId, "mixed-release-installation", "mixed-release-token");
        UUID requestId = requestNotification.request(RequestFcmNotificationCommand.builder()
            .memberIds(List.of(memberId))
            .title("공지")
            .body("본문")
            .data(Map.of("noticeId", "42"))
            .deepLink("umc://notice/42")
            .build()).requestId();
        String payload = jdbc.queryForObject("""
            SELECT payload FROM event_outbox
            WHERE event_type = 'notification.fcm.requested' AND payload::jsonb ->> 'requestId' = ?
            """, String.class, requestId.toString());
        assertThat(objectMapper.readTree(payload).has("notificationType")).isFalse();
        // 명시적 대상과 활성 토큰 한 개를 가진 요청에 대해 구버전의 푸시 배치 생성 경로를 재현한다.
        ApplicationEventPublisher legacyPublisher = event -> {
            assertThat(event).isInstanceOfSatisfying(FcmNotificationRequestedEvent.class, request -> {
                assertThat(request.requestId()).isEqualTo(requestId);
                List<Long> tokenIds = loadFcmPort.listActiveByMemberIds(request.memberIds()).stream()
                    .map(FcmToken::getId)
                    .toList();
                assertThat(tokenIds).hasSize(1);
                outboxPublisher.publish(FcmSendBatchRequestedEvent.create(requestId, tokenIds, request));
            });
        };
        var legacyRelay = new EventOutboxRelayService(loadOutbox, saveOutbox, deserializer, legacyPublisher,
            transactionManager, tracerProvider, relayMetrics, 3);
        var group = OutboxProcessingGroup.BACKGROUND;

        // when
        assertThat(legacyRelay.relay(group, routingPolicy.selectionFor(group), 1)).isEqualTo(1);

        // then
        assertThat(requestStatus(requestId)).isEqualTo("PUBLISHED");
        assertThat(historyCount(requestId)).isZero();
        assertThat(batchCount(requestId)).isEqualTo(1);
        String batchPayload = jdbc.queryForObject("""
            SELECT payload FROM event_outbox
            WHERE event_type = 'notification.fcm.batch.requested' AND payload::jsonb ->> 'requestId' = ?
            """, String.class, requestId.toString());
        var batch = objectMapper.readValue(batchPayload, FcmSendBatchRequestedEvent.class);
        assertThat(batch.data()).containsEntry("noticeId", "42");
        assertThat(batch.deepLink()).isEqualTo("umc://notice/42");
    }

    private long historyCount(UUID requestId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM notification_history WHERE request_id = ?", Long.class,
            requestId);
    }

    private long batchCount(UUID requestId) {
        return jdbc.queryForObject("""
            SELECT COUNT(*) FROM event_outbox
            WHERE event_type = 'notification.fcm.batch.requested' AND payload::jsonb ->> 'requestId' = ?
            """, Long.class, requestId.toString());
    }

    private String requestStatus(UUID requestId) {
        return jdbc.queryForObject("""
            SELECT status FROM event_outbox
            WHERE event_type = 'notification.fcm.requested' AND payload::jsonb ->> 'requestId' = ?
            """, String.class, requestId.toString());
    }

    private static void awaitLatch(CountDownLatch latch) {
        try {
            if (!latch.await(25, TimeUnit.SECONDS)) {
                throw new IllegalStateException("동시 저장 테스트 대기 시간 초과");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

}
