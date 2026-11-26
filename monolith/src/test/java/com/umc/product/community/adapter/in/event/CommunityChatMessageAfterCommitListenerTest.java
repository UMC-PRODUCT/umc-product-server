package com.umc.product.community.adapter.in.event;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;

import java.time.Instant;
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

import com.umc.product.chat.application.event.ChatRealtimeAfterCommitEvent;
import com.umc.product.chat.domain.MessageContentType;
import com.umc.product.chat.domain.event.ChatMessageCreatedEvent;
import com.umc.product.chat.domain.event.ChatMessageDeletedEvent;
import com.umc.product.chat.domain.event.ChatMessageReactionChangedEvent;
import com.umc.product.chat.domain.event.ChatMessageSnapshot;
import com.umc.product.chat.domain.event.ChatMessageUpdatedEvent;
import com.umc.product.chat.domain.event.ChatReadUpdatedEvent;
import com.umc.product.chat.domain.event.ChatRealtimeEvent;
import com.umc.product.community.application.event.CommunityThreadRealtimeAfterCommitEvent;
import com.umc.product.community.application.port.in.realtime.RelayCommunityThreadRealtimeEventUseCase;
import com.umc.product.community.domain.event.CommunityThreadDeletedEvent;
import com.umc.product.community.domain.event.CommunityThreadLifecycleEvent.RealtimeOnly;
import com.umc.product.community.domain.event.CommunityThreadMemberKickedEvent;
import com.umc.product.community.domain.event.CommunityThreadMemberLeftEvent;
import com.umc.product.community.domain.event.CommunityThreadUpdatedEvent;

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
    @DisplayName("채팅 상태 변경이 커밋된 경우에만 이벤트별 전파 작업을 등록한다")
    void submitsOnlyAfterCommit(boolean committed) {
        // given
        ChatMessageSnapshot snapshot = new ChatMessageSnapshot(
            1L, 2L, 3L, MessageContentType.TEXT, "수정된 내용", List.of(), null, null, List.of(), null, null, null
        );
        ChatMessageUpdatedEvent messageUpdated = new ChatMessageUpdatedEvent(null, null, snapshot);
        ChatMessageDeletedEvent messageDeleted = new ChatMessageDeletedEvent(null, null, snapshot);
        ChatMessageReactionChangedEvent reactionChanged = ChatMessageReactionChangedEvent.of(2L, 1L, 3L, "👍", true);
        ChatReadUpdatedEvent readUpdated = ChatReadUpdatedEvent.of(2L, 3L, 1L);
        List<ChatRealtimeEvent> events = List.of(messageCreated, messageUpdated, messageDeleted, reactionChanged,
            readUpdated);
        Instant joinedAt = Instant.parse("2026-11-25T12:00:00Z");
        Instant changedAt = joinedAt.plusSeconds(60);
        List<Long> activeMembers = List.of(2L, 3L);
        CommunityThreadUpdatedEvent threadUpdated = CommunityThreadUpdatedEvent.of(1L, 2L, changedAt);
        CommunityThreadDeletedEvent threadDeleted = CommunityThreadDeletedEvent.of(1L, 2L, activeMembers, changedAt);
        CommunityThreadMemberKickedEvent memberKicked =
            CommunityThreadMemberKickedEvent.of(1L, 2L, 3L, activeMembers, changedAt);
        CommunityThreadMemberLeftEvent memberLeft =
            CommunityThreadMemberLeftEvent.of(1L, 3L, activeMembers, joinedAt, changedAt);
        List<RealtimeOnly> threadEvents = List.of(threadUpdated, threadDeleted, memberKicked, memberLeft);
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
                events.forEach(event -> context.publishEvent(new ChatRealtimeAfterCommitEvent(event)));
                threadEvents.forEach(event -> context.publishEvent(new CommunityThreadRealtimeAfterCommitEvent(event)));
                then(executor).shouldHaveNoInteractions();
                TransactionSynchronizationUtils.triggerAfterCompletion(committed
                    ? TransactionSynchronization.STATUS_COMMITTED : TransactionSynchronization.STATUS_ROLLED_BACK);

                // then
                then(relayUseCase).shouldHaveNoInteractions();
                if (committed) {
                    ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
                    then(executor).should(times(events.size() + threadEvents.size())).execute(task.capture());
                    task.getAllValues().forEach(Runnable::run);
                    then(relayUseCase).should().relay(messageCreated);
                    then(relayUseCase).should().relay(messageUpdated);
                    then(relayUseCase).should().relay(messageDeleted);
                    then(relayUseCase).should().relay(reactionChanged);
                    then(relayUseCase).should().relay(readUpdated);
                    then(relayUseCase).should().relay(threadUpdated);
                    then(relayUseCase).should().relay(threadDeleted);
                    then(relayUseCase).should().relay(memberKicked);
                    then(relayUseCase).should().relay(memberLeft);
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
            new CommunityChatMessageAfterCommitListener(relayUseCase, executor, false);

        // when, then
        assertThatCode(() -> listener.onChatRealtimeEvent(new ChatRealtimeAfterCommitEvent(messageCreated)))
            .doesNotThrowAnyException();
        then(relayUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("실시간 전송 실패가 비동기 작업 밖으로 전파되지 않는다")
    void handlesRelayFailure() {
        // given
        willThrow(new IllegalStateException("브로커 전송 실패")).given(relayUseCase).relay(messageCreated);
        CommunityChatMessageAfterCommitListener listener =
            new CommunityChatMessageAfterCommitListener(relayUseCase, executor, true);
        ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);

        // when
        listener.onChatRealtimeEvent(new ChatRealtimeAfterCommitEvent(messageCreated));

        // then
        then(executor).should().execute(task.capture());
        assertThatCode(task.getValue()::run).doesNotThrowAnyException();
        then(relayUseCase).should().relay(messageCreated);
    }
}
