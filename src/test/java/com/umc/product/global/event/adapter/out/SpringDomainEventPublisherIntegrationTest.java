package com.umc.product.global.event.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.global.event.application.port.out.DomainEventPublisher;
import com.umc.product.global.event.domain.DomainEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionalEventListenerFactory;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@DisplayName("SpringDomainEventPublisher local pub/sub")
class SpringDomainEventPublisherIntegrationTest {

    @Test
    @DisplayName("트랜잭션 commit 이후 같은 JVM의 여러 subscriber가 이벤트를 수신한다")
    void commit_이후_local_fanout() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            DomainEventPublisher publisher = context.getBean(DomainEventPublisher.class);
            TransactionTemplate tx = context.getBean(TransactionTemplate.class);
            FirstSubscriber first = context.getBean(FirstSubscriber.class);
            SecondSubscriber second = context.getBean(SecondSubscriber.class);
            TestDomainEvent event = TestDomainEvent.create("commit");

            tx.executeWithoutResult(status -> publisher.publish(event));

            assertThat(first.handled()).containsExactly(event.eventId());
            assertThat(second.handled()).containsExactly(event.eventId());
        }
    }

    @Test
    @DisplayName("트랜잭션 rollback 시 AFTER_COMMIT subscriber는 이벤트를 수신하지 않는다")
    void rollback_시_after_commit_listener_미실행() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            DomainEventPublisher publisher = context.getBean(DomainEventPublisher.class);
            TransactionTemplate tx = context.getBean(TransactionTemplate.class);
            FirstSubscriber first = context.getBean(FirstSubscriber.class);
            SecondSubscriber second = context.getBean(SecondSubscriber.class);
            TestDomainEvent event = TestDomainEvent.create("rollback");

            assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
                publisher.publish(event);
                throw new IllegalStateException("rollback");
            })).isInstanceOf(IllegalStateException.class);

            assertThat(first.handled()).isEmpty();
            assertThat(second.handled()).isEmpty();
        }
    }

    @Configuration
    static class TestConfig {

        @Bean
        DomainEventPublisher domainEventPublisher(ApplicationEventPublisher publisher) {
            return new SpringDomainEventPublisher(publisher);
        }

        @Bean
        PlatformTransactionManager transactionManager() {
            return new LocalTransactionManager();
        }

        @Bean
        TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
            return new TransactionTemplate(transactionManager);
        }

        @Bean
        TransactionalEventListenerFactory transactionalEventListenerFactory() {
            return new TransactionalEventListenerFactory();
        }

        @Bean
        FirstSubscriber firstSubscriber() {
            return new FirstSubscriber();
        }

        @Bean
        SecondSubscriber secondSubscriber() {
            return new SecondSubscriber();
        }
    }

    public static class FirstSubscriber {

        private final List<UUID> handled = new CopyOnWriteArrayList<>();

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void handle(TestDomainEvent event) {
            handled.add(event.eventId());
        }

        List<UUID> handled() {
            return handled;
        }
    }

    public static class SecondSubscriber {

        private final List<UUID> handled = new CopyOnWriteArrayList<>();

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void handle(TestDomainEvent event) {
            handled.add(event.eventId());
        }

        List<UUID> handled() {
            return handled;
        }
    }

    private record TestDomainEvent(
        UUID eventId,
        Instant occurredAt,
        String name
    ) implements DomainEvent {

        static TestDomainEvent create(String name) {
            return new TestDomainEvent(UUID.randomUUID(), Instant.now(), name);
        }

        @Override
        public String eventType() {
            return "test.domain.event";
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
