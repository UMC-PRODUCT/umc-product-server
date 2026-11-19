package com.umc.product.community.adapter.in.event;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.event.TransactionalEventListenerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import com.umc.product.chat.application.event.ChatMessageCreatedAfterCommitEvent;
import com.umc.product.chat.domain.MessageContentType;
import com.umc.product.chat.domain.event.ChatMessageCreatedEvent;
import com.umc.product.community.application.port.in.realtime.RelayCommunityThreadRealtimeEventUseCase;

@ExtendWith(MockitoExtension.class)
class CommunityChatMessageAfterCommitListenerTest {

    @Mock
    private RelayCommunityThreadRealtimeEventUseCase relayUseCase;

    @Mock
    private Executor executor;

    private final ChatMessageCreatedEvent messageCreated = new ChatMessageCreatedEvent(
        null, null, 1L, 2L, 3L, MessageContentType.TEXT, "테스트 메시지", List.of()
    );

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("메시지 저장이 커밋된 경우에만 전송 작업을 등록한다")
    void submitsOnlyAfterCommit(boolean committed) {
        // given
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(TransactionalEventListenerFactory.class);
            context.registerBean("communityChatRealtimeExecutor", Executor.class, () -> executor);
            context.registerBean(RelayCommunityThreadRealtimeEventUseCase.class, () -> relayUseCase);
            context.register(CommunityChatMessageAfterCommitListener.class);
            context.refresh();
            TransactionSynchronizationManager.setActualTransactionActive(true);
            TransactionSynchronizationManager.initSynchronization();
            try {
                // when
                context.publishEvent(new ChatMessageCreatedAfterCommitEvent(messageCreated));
                then(executor).shouldHaveNoInteractions();
                TransactionSynchronizationUtils.triggerAfterCompletion(committed
                    ? TransactionSynchronization.STATUS_COMMITTED : TransactionSynchronization.STATUS_ROLLED_BACK);

                // then
                then(relayUseCase).shouldHaveNoInteractions();
                if (committed) {
                    ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
                    then(executor).should().execute(task.capture());
                    task.getValue().run();
                    then(relayUseCase).should().relay(messageCreated);
                } else {
                    then(executor).shouldHaveNoInteractions();
                }
            } finally {
                TransactionSynchronizationManager.clear();
            }
        }
    }

    @Test
    @DisplayName("전송 작업이 거절되어도 커밋 후 호출에 예외를 전파하지 않는다")
    void handlesRejectedTask() {
        // given
        willThrow(new RejectedExecutionException("대기열 포화")).given(executor).execute(any());
        CommunityChatMessageAfterCommitListener listener =
            new CommunityChatMessageAfterCommitListener(relayUseCase, executor);

        // when, then
        assertThatCode(() -> listener.onMessageCreated(new ChatMessageCreatedAfterCommitEvent(messageCreated)))
            .doesNotThrowAnyException();
        then(relayUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("실시간 전송 실패가 비동기 작업 밖으로 전파되지 않는다")
    void handlesRelayFailure() {
        // given
        willThrow(new IllegalStateException("브로커 전송 실패")).given(relayUseCase).relay(messageCreated);
        CommunityChatMessageAfterCommitListener listener =
            new CommunityChatMessageAfterCommitListener(relayUseCase, executor);
        ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);

        // when
        listener.onMessageCreated(new ChatMessageCreatedAfterCommitEvent(messageCreated));

        // then
        then(executor).should().execute(task.capture());
        assertThatCode(task.getValue()::run).doesNotThrowAnyException();
        then(relayUseCase).should().relay(messageCreated);
    }
}
