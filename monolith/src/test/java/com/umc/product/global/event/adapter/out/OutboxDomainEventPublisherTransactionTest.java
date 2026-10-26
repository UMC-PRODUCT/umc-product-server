package com.umc.product.global.event.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.config.OutboxRoutingConfig;
import com.umc.product.global.event.application.event.RealtimeOutboxCommittedEvent;
import com.umc.product.global.event.application.port.out.DomainEventPublisher;
import com.umc.product.global.event.application.port.out.SaveEventOutboxPort;
import com.umc.product.global.event.domain.DomainEvent;

import io.micrometer.tracing.Tracer;

@ExtendWith(MockitoExtension.class)
@DisplayName("Outbox 저장과 실시간 실행 신호의 트랜잭션 경계")
class OutboxDomainEventPublisherTransactionTest {

    @Mock
    private SaveEventOutboxPort savePort;

    private final List<Object> signals = new ArrayList<>();
    private final LocalTransactionManager transactionManager = new LocalTransactionManager();
    private DomainEventPublisher publisher;
    private TransactionTemplate transaction;

    @BeforeEach
    void setUp() {
        transaction = new TransactionTemplate(transactionManager);
        OutboxCommitSignalPublisher signalPublisher = new OutboxCommitSignalPublisher(
            new OutboxRoutingConfig().outboxEventRoutingPolicy(), signals::add
        );
        publisher = createPublisher(signalPublisher);
    }

    @Test
    @DisplayName("외부 트랜잭션이 커밋된 뒤에만 실시간 실행 신호를 발행한다")
    void 커밋_이후_신호() {
        // given
        TestEvent event = TestEvent.create("chat.message.created");

        // when
        transaction.executeWithoutResult(status -> {
            publisher.publish(event);
            assertThat(signals).isEmpty();
        });

        // then
        assertThat(transactionManager.commits).hasValue(1);
        assertThat(signals).singleElement().isInstanceOf(RealtimeOutboxCommittedEvent.class);
    }

    @Test
    @DisplayName("외부 트랜잭션이 롤백되면 실행 신호를 발행하지 않는다")
    void 롤백_신호_없음() {
        // when
        transaction.executeWithoutResult(status -> {
            publisher.publish(TestEvent.create("chat.message.created"));
            status.setRollbackOnly();
        });

        // then
        assertThat(signals).isEmpty();
        assertThat(transactionManager.commits).hasValue(0);
    }

    @Test
    @DisplayName("같은 트랜잭션의 단건과 일괄 발행은 실행 신호 하나로 합친다")
    void 트랜잭션별_신호_병합() {
        // when
        transaction.executeWithoutResult(status -> {
            publisher.publish(TestEvent.create("chat.message.created"));
            publisher.publishAll(List.of(
                TestEvent.create("chat.message.updated"), TestEvent.create("community.thread.invited")
            ));
        });

        // then
        assertThat(signals).hasSize(1);
    }

    @Test
    @DisplayName("백그라운드 이벤트와 빈 일괄 발행은 실행 신호를 만들지 않는다")
    void 백그라운드와_빈_목록() {
        // when
        publisher.publish(TestEvent.create("notification.fcm.requested"));
        publisher.publishAll(List.of());

        // then
        assertThat(signals).isEmpty();
    }

    @Test
    @DisplayName("혼합 일괄 발행은 기존 라우팅 정책으로 실시간 이벤트를 찾아 신호를 만든다")
    void 혼합_일괄_발행() {
        // when
        publisher.publishAll(List.of(
            TestEvent.create("notification.fcm.requested"), TestEvent.create("community.thread.updated")
        ));

        // then
        assertThat(signals).hasSize(1);
    }

    @Test
    @DisplayName("외부 트랜잭션이 없어도 자체 저장 트랜잭션의 커밋 이후에 신호를 발행한다")
    void 자체_트랜잭션_커밋() {
        // given
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
            assertThat(signals).isEmpty();
            assertThat(transactionManager.commits).hasValue(0);
            return null;
        }).when(savePort).save(any());

        // when
        publisher.publish(TestEvent.create("chat.message.created"));

        // then
        assertThat(transactionManager.commits).hasValue(1);
        assertThat(signals).hasSize(1);
    }

    @Test
    @DisplayName("저장 실패는 전파하고 실행 신호는 발행하지 않는다")
    void 저장_실패() {
        // given
        doThrow(new IllegalStateException("저장 실패")).when(savePort).save(any());

        // when / then
        assertThatThrownBy(() -> publisher.publish(TestEvent.create("chat.message.created")))
            .isInstanceOf(IllegalStateException.class);
        assertThat(signals).isEmpty();
        assertThat(transactionManager.commits).hasValue(0);
    }

    @Test
    @DisplayName("일괄 저장 실패는 실행 신호를 남기지 않는다")
    void 일괄_저장_실패() {
        // given
        doThrow(new IllegalStateException("저장 실패")).when(savePort).saveAll(any());

        // when / then
        assertThatThrownBy(() -> publisher.publishAll(List.of(TestEvent.create("chat.message.created"))))
            .isInstanceOf(IllegalStateException.class);
        assertThat(signals).isEmpty();
    }

    @Test
    @DisplayName("커밋 이후 신호 발행 실패는 성공한 저장 요청의 오류로 전파하지 않는다")
    void 신호_실패_격리() {
        // given
        OutboxCommitSignalPublisher failingSignalPublisher = new OutboxCommitSignalPublisher(
            new OutboxRoutingConfig().outboxEventRoutingPolicy(), event -> {
                throw new IllegalStateException("신호 발행 실패");
            }
        );
        DomainEventPublisher failingPublisher = createPublisher(failingSignalPublisher);

        // when / then
        assertThatCode(() -> failingPublisher.publish(TestEvent.create("chat.message.created")))
            .doesNotThrowAnyException();
        assertThat(transactionManager.commits).hasValue(1);
    }

    @Test
    @DisplayName("연속된 트랜잭션은 각각 독립적인 실행 신호를 만든다")
    void 연속_트랜잭션() {
        // when
        publisher.publish(TestEvent.create("chat.message.created"));
        publisher.publish(TestEvent.create("chat.message.created"));

        // then
        assertThat(signals).hasSize(2);
    }

    @Test
    @DisplayName("독립 트랜잭션의 커밋 신호는 외부 트랜잭션 롤백과 분리된다")
    void 독립_트랜잭션_신호() {
        // given
        TransactionTemplate requiresNew = new TransactionTemplate(transactionManager);
        requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // when
        transaction.executeWithoutResult(status -> {
            publisher.publish(TestEvent.create("chat.message.created"));
            requiresNew.executeWithoutResult(inner -> publisher.publish(TestEvent.create("chat.message.created")));
            assertThat(signals).hasSize(1);
            status.setRollbackOnly();
        });

        // then
        assertThat(signals).hasSize(1);
    }

    @Test
    @DisplayName("트랜잭션 동기화가 비활성화되면 커밋 전 신호 대신 복구 폴링에 맡긴다")
    void 동기화_비활성화() {
        // given
        transactionManager.setTransactionSynchronization(AbstractPlatformTransactionManager.SYNCHRONIZATION_NEVER);

        // when
        publisher.publish(TestEvent.create("chat.message.created"));

        // then
        assertThat(transactionManager.commits).hasValue(1);
        assertThat(signals).isEmpty();
    }

    private DomainEventPublisher createPublisher(OutboxCommitSignalPublisher signalPublisher) {
        OutboxDomainEventPublisher target = new OutboxDomainEventPublisher(
            savePort, new EventPayloadSerializer(new ObjectMapper().findAndRegisterModules()), Tracer.NOOP,
            signalPublisher
        );
        ProxyFactory factory = new ProxyFactory(target);
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(transactionManager);
        interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        factory.addAdvice(interceptor);
        return (DomainEventPublisher) factory.getProxy();
    }

    private record TestEvent(UUID eventId, Instant occurredAt, String eventType) implements DomainEvent {

        static TestEvent create(String eventType) {
            return new TestEvent(UUID.randomUUID(), Instant.now(), eventType);
        }
    }

    private static class LocalTransactionManager extends AbstractPlatformTransactionManager {

        private final ThreadLocal<TransactionState> current = new ThreadLocal<>();
        private final AtomicInteger commits = new AtomicInteger();

        @Override
        protected Object doGetTransaction() {
            TransactionState existing = current.get();
            return existing == null ? new TransactionState() : existing;
        }

        @Override
        protected boolean isExistingTransaction(Object transaction) {
            return ((TransactionState) transaction).active;
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            TransactionState state = (TransactionState) transaction;
            state.active = true;
            current.set(state);
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            commits.incrementAndGet();
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
        }

        @Override
        protected Object doSuspend(Object transaction) {
            current.remove();
            // suspend된 외부 트랜잭션을 활성 트랜잭션으로 감지하지 않도록 분리한다.
            TransactionState state = (TransactionState) transaction;
            state.active = false;
            return state;
        }

        @Override
        protected void doResume(Object transaction, Object suspendedResources) {
            TransactionState state = (TransactionState) suspendedResources;
            state.active = true;
            current.set(state);
        }

        @Override
        protected void doCleanupAfterCompletion(Object transaction) {
            ((TransactionState) transaction).active = false;
            current.remove();
        }

        private static class TransactionState {

            private boolean active;
        }
    }
}
