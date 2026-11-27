package com.umc.product.notification.application.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import com.umc.product.global.event.adapter.out.EventPayloadDeserializer;
import com.umc.product.global.event.application.port.out.DomainEventPublisher;
import com.umc.product.global.event.application.port.out.LoadEventOutboxPort;
import com.umc.product.global.event.application.port.out.SaveEventOutboxPort;
import com.umc.product.global.event.application.service.EventOutboxRelayMetrics;
import com.umc.product.global.event.application.service.EventOutboxRelayService;
import com.umc.product.global.event.application.service.OutboxEventRoutingPolicy;
import com.umc.product.global.event.application.service.OutboxProcessingGroup;
import com.umc.product.member.application.port.in.command.ManageMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.DeleteMemberCommand;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.notification.application.port.in.RequestFcmNotificationUseCase;
import com.umc.product.notification.application.port.in.command.RecordNotificationHistoryUseCase;
import com.umc.product.notification.application.port.in.dto.RequestFcmNotificationCommand;
import com.umc.product.notification.application.port.out.LoadFcmPort;
import com.umc.product.notification.application.port.out.SaveNotificationHistoryPort;
import com.umc.product.notification.domain.FcmToken;
import com.umc.product.notification.domain.NotificationHistory;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.FcmTokenFixture;
import com.umc.product.support.fixture.MemberFixture;
import com.umc.product.support.fixture.SchoolFixture;

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
    @Autowired
    private SchoolFixture schools;
    @Autowired
    private SaveMemberPort saveMemberPort;
    @Autowired
    private ManageMemberUseCase manageMemberUseCase;
    @Autowired
    private RecordNotificationHistoryUseCase recordNotificationHistoryUseCase;

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
                awaitBlocked(secondBackendPid.get(10, TimeUnit.SECONDS));
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
            .recordHistory(true)
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
        makeRequestRetryable(requestId);

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
    @DisplayName("이력 저장이 실패하면 푸시를 보류하고 재시도로 이력을 커밋한 뒤에만 발송한다")
    void 이력_저장_실패_후_재시도에서_이력이_커밋되면_푸시를_발송한다() throws Exception {
        // given
        Long memberId = members.일반("이력저장재시도회원").getId();
        tokens.FCM_토큰(memberId, "history-retry-installation", "history-retry-token");
        UUID requestId = requestNotification.request(RequestFcmNotificationCommand.builder()
            .memberIds(List.of(memberId))
            .title("공지")
            .body("본문")
            .recordHistory(true)
            .build()).requestId();
        var group = OutboxProcessingGroup.BACKGROUND;
        var selection = routingPolicy.selectionFor(group);
        ApplicationEventPublisher rejectHistoryInsert = event -> {
            // 이력 INSERT를 실패시키고, 해당 트랜잭션이 롤백될 때 제약도 함께 제거한다.
            jdbc.execute("""
                ALTER TABLE notification_history ADD CONSTRAINT reject_history_insert CHECK (false) NOT VALID
                """);
            springEvents.publishEvent(event);
        };
        var failingRelay = new EventOutboxRelayService(loadOutbox, saveOutbox, deserializer, rejectHistoryInsert,
            transactionManager, tracerProvider, relayMetrics, 3);

        // when
        assertThat(failingRelay.relay(group, selection, 1)).isEqualTo(1);

        // then
        assertThat(requestStatus(requestId)).isEqualTo("PENDING");
        assertThat(jdbc.queryForObject("""
            SELECT last_error FROM event_outbox
            WHERE event_type = 'notification.fcm.requested' AND payload::jsonb ->> 'requestId' = ?
            """, String.class, requestId.toString())).contains("reject_history_insert");
        assertThat(historyCount(requestId)).isZero();
        assertThat(batchCount(requestId)).isZero();
        verifyNoInteractions(firebaseMessaging);

        // when / then
        makeRequestRetryable(requestId);
        assertThat(relay.relay(group, selection, 1)).isEqualTo(1);
        assertThat(requestStatus(requestId)).isEqualTo("PUBLISHED");
        assertThat(batchCount(requestId)).isEqualTo(1);
        verifyNoInteractions(firebaseMessaging);

        SendResponse sendResponse = mock(SendResponse.class);
        given(sendResponse.isSuccessful()).willReturn(true);
        BatchResponse batchResponse = mock(BatchResponse.class);
        given(batchResponse.getResponses()).willReturn(List.of(sendResponse));
        given(batchResponse.getSuccessCount()).willReturn(1);
        given(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class))).willAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            assertThat(historyRecipientIds(requestId)).containsExactly(memberId);
            return batchResponse;
        });

        assertThat(relay.relay(group, selection, 1)).isEqualTo(1);
        verify(firebaseMessaging).sendEachForMulticast(any(MulticastMessage.class));
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
            .recordHistory(true)
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

    @Test
    @DisplayName("탈퇴와 발송이 겹쳐도 탈퇴한 수신자만 제외되고 다른 수신자의 이력은 남는다")
    void 탈퇴한_회원의_수신_이력만_삭제한다() throws Exception {
        // given
        Long withdrawnId = createWithdrawableMember();
        Long otherId = members.일반("다른수신자").getId();
        List<Long> recipients = List.of(withdrawnId, otherId);
        var existing = new NotificationHistory(UUID.randomUUID(), "기존 알림", "본문", null, null, Instant.now());
        var pending = new NotificationHistory(UUID.randomUUID(), "처리 중 알림", "본문", null, null, Instant.now());
        recordNotificationHistoryUseCase.recordHistory(existing, recipients);
        assertThat(historyCount(existing.requestId())).isEqualTo(2);

        var withdrawalFinished = new CountDownLatch(1);
        var allowWithdrawalCommit = new CountDownLatch(1);
        var writerPid = new CompletableFuture<Integer>();
        var transaction = new TransactionTemplate(transactionManager);
        transaction.setTimeout(30);

        // when
        try (var workers = Executors.newFixedThreadPool(2)) {
            var withdrawal = workers.submit(() -> transaction.executeWithoutResult(status -> {
                manageMemberUseCase.deleteMember(DeleteMemberCommand.builder().memberId(withdrawnId).build());
                withdrawalFinished.countDown();
                awaitLatch(allowWithdrawalCommit);
            }));
            var writer = workers.submit(() -> {
                awaitLatch(withdrawalFinished);
                transaction.executeWithoutResult(status -> {
                    writerPid.complete(jdbc.queryForObject("SELECT pg_backend_pid()", Integer.class));
                    recordNotificationHistoryUseCase.recordHistory(pending, recipients);
                });
            });

            try {
                awaitBlocked(writerPid.get(10, TimeUnit.SECONDS));
            } finally {
                allowWithdrawalCommit.countDown();
            }
            withdrawal.get(10, TimeUnit.SECONDS);
            writer.get(10, TimeUnit.SECONDS);
        }

        // then
        assertThat(jdbc.queryForList("""
            SELECT member_id FROM notification_history WHERE request_id IN (?, ?)
            """, Long.class, existing.requestId(), pending.requestId())).containsExactly(otherId, otherId);
        assertThat(historyCount(existing.requestId())).isEqualTo(1);
        assertThat(historyCount(pending.requestId())).isEqualTo(1);
    }

    @Test
    @DisplayName("알림 저장이 먼저 시작되면 탈퇴는 저장이 끝난 뒤 탈퇴 회원의 이력만 삭제한다")
    void 저장이_먼저_시작된_알림도_탈퇴할_때_삭제한다() throws Exception {
        // given
        Long withdrawnId = createWithdrawableMember();
        Long otherId = members.일반("다른수신자").getId();
        List<Long> recipients = List.of(withdrawnId, otherId);
        var existing = new NotificationHistory(UUID.randomUUID(), "기존 알림", "본문", null, null, Instant.now());
        var pending = new NotificationHistory(UUID.randomUUID(), "처리 중 알림", "본문", null, null, Instant.now());
        recordNotificationHistoryUseCase.recordHistory(existing, recipients);

        var historySaved = new CountDownLatch(1);
        var allowWriterCommit = new CountDownLatch(1);
        var withdrawalPid = new CompletableFuture<Integer>();
        var transaction = new TransactionTemplate(transactionManager);
        transaction.setTimeout(30);

        // when
        try (var workers = Executors.newFixedThreadPool(2)) {
            var writer = workers.submit(() -> transaction.executeWithoutResult(status -> {
                recordNotificationHistoryUseCase.recordHistory(pending, recipients);
                assertThat(historyRecipientIds(pending.requestId())).containsExactlyInAnyOrderElementsOf(recipients);
                historySaved.countDown();
                awaitLatch(allowWriterCommit);
            }));
            var withdrawal = workers.submit(() -> {
                awaitLatch(historySaved);
                transaction.executeWithoutResult(status -> {
                    withdrawalPid.complete(jdbc.queryForObject("SELECT pg_backend_pid()", Integer.class));
                    manageMemberUseCase.deleteMember(DeleteMemberCommand.builder().memberId(withdrawnId).build());
                });
            });

            try {
                awaitBlocked(withdrawalPid.get(10, TimeUnit.SECONDS));
            } finally {
                allowWriterCommit.countDown();
            }
            writer.get(10, TimeUnit.SECONDS);
            withdrawal.get(10, TimeUnit.SECONDS);
        }

        // then
        assertThat(historyRecipientIds(existing.requestId())).containsExactly(otherId);
        assertThat(historyRecipientIds(pending.requestId())).containsExactly(otherId);
    }

    @Test
    @DisplayName("탈퇴 트랜잭션이 롤백되면 회원과 그 회원의 수신 알림 이력이 복구된다")
    void 탈퇴_롤백_시_회원과_수신_알림_이력이_복구된다() {
        // given
        Long memberId = createWithdrawableMember();
        Long otherId = members.일반("다른수신자").getId();
        List<Long> recipients = List.of(memberId, otherId);
        var history = new NotificationHistory(UUID.randomUUID(), "기존 알림", "본문", null, null, Instant.now());
        recordNotificationHistoryUseCase.recordHistory(history, recipients);
        var transaction = new TransactionTemplate(transactionManager);

        // when
        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            manageMemberUseCase.deleteMember(DeleteMemberCommand.builder().memberId(memberId).build());
            entityManager.flush();
            assertThat(memberExists(memberId)).isFalse();
            assertThat(historyRecipientIds(history.requestId())).containsExactly(otherId);
            throw new IllegalStateException("탈퇴 롤백 테스트");
        })).isInstanceOf(IllegalStateException.class).hasMessage("탈퇴 롤백 테스트");

        // then
        assertThat(memberExists(memberId)).isTrue();
        assertThat(historyRecipientIds(history.requestId())).containsExactlyInAnyOrderElementsOf(recipients);
    }

    @Test
    @DisplayName("이력 기록을 요청하지 않은 일반 푸시는 공지 딥링크가 있어도 이력 없이 배치를 등록한다")
    void 이력_기록을_요청하지_않으면_푸시_배치만_등록한다() throws Exception {
        // given
        Long memberId = members.일반("일반푸시수신자").getId();
        tokens.FCM_토큰(memberId, "push-only-installation", "push-only-token");
        UUID requestId = requestNotification.request(RequestFcmNotificationCommand.builder()
            .memberIds(List.of(memberId))
            .title("일반 푸시")
            .body("본문")
            .deepLink("umc://notice/42")
            .build()).requestId();
        String payload = jdbc.queryForObject("""
            SELECT payload FROM event_outbox
            WHERE event_type = 'notification.fcm.requested' AND payload::jsonb ->> 'requestId' = ?
            """, String.class, requestId.toString());
        var group = OutboxProcessingGroup.BACKGROUND;

        // when
        assertThat(relay.relay(group, routingPolicy.selectionFor(group), 1)).isEqualTo(1);

        // then
        assertThat(objectMapper.readTree(payload).has("recordHistory")).isFalse();
        assertThat(requestStatus(requestId)).isEqualTo("PUBLISHED");
        assertThat(batchCount(requestId)).isEqualTo(1);
        assertThat(historyCount(requestId)).isZero();
    }

    private Long createWithdrawableMember() {
        Long schoolId = schools.학교("알림테스트학교").getId();
        return saveMemberPort.save(
            Member.create("탈퇴회원", "탈퇴회원", "inbox-withdrawn@example.com", schoolId, null)
        ).getId();
    }

    private List<Long> historyRecipientIds(UUID requestId) {
        return jdbc.queryForList("SELECT member_id FROM notification_history WHERE request_id = ?", Long.class,
            requestId);
    }

    private boolean memberExists(Long memberId) {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM member WHERE id = ?)",
            Boolean.class, memberId));
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

    private void awaitBlocked(int pid) {
        await().atMost(Duration.ofSeconds(10)).until(() -> Boolean.TRUE.equals(jdbc.queryForObject(
            "SELECT cardinality(pg_blocking_pids(?)) > 0", Boolean.class, pid
        )));
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

    private void makeRequestRetryable(UUID requestId) {
        assertThat(jdbc.update("""
            UPDATE event_outbox SET next_attempt_at = TIMESTAMPTZ '2000-01-01 00:00:00+00'
            WHERE event_type = 'notification.fcm.requested' AND payload::jsonb ->> 'requestId' = ?
            """, requestId.toString())).isEqualTo(1);
    }

}
