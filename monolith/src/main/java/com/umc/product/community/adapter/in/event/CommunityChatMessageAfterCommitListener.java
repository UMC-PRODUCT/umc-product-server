package com.umc.product.community.adapter.in.event;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.umc.product.chat.application.event.ChatRealtimeAfterCommitEvent;
import com.umc.product.chat.domain.event.ChatMessageCreatedEvent;
import com.umc.product.chat.domain.event.ChatMessageDeletedEvent;
import com.umc.product.chat.domain.event.ChatMessageReactionChangedEvent;
import com.umc.product.chat.domain.event.ChatMessageUpdatedEvent;
import com.umc.product.chat.domain.event.ChatReadUpdatedEvent;
import com.umc.product.chat.domain.event.ChatRealtimeEvent;
import com.umc.product.community.application.port.in.realtime.RelayCommunityThreadRealtimeEventUseCase;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CommunityChatMessageAfterCommitListener {

    private final RelayCommunityThreadRealtimeEventUseCase relayUseCase;
    private final Executor realtimeExecutor;
    private final boolean diagnosticsEnabled;

    public CommunityChatMessageAfterCommitListener(
        RelayCommunityThreadRealtimeEventUseCase relayUseCase,
        @Qualifier("communityChatRealtimeExecutor") Executor realtimeExecutor,
        @Value("${app.community.chat-realtime.diagnostics-enabled:false}") boolean diagnosticsEnabled
    ) {
        this.relayUseCase = relayUseCase;
        this.realtimeExecutor = realtimeExecutor;
        this.diagnosticsEnabled = diagnosticsEnabled;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChatRealtimeEvent(ChatRealtimeAfterCommitEvent afterCommitEvent) {
        ChatRealtimeEvent event = afterCommitEvent.event();
        try {
            if (diagnosticsEnabled) {
                long submittedAtMs = System.currentTimeMillis();
                long submittedNanos = System.nanoTime();
                realtimeExecutor.execute(() -> relayWithTiming(event, submittedAtMs, submittedNanos));
            } else {
                realtimeExecutor.execute(() -> relay(event));
            }
        } catch (RejectedExecutionException exception) {
            log.warn(
                "채팅 실시간 전송 작업 등록 거절: eventId={}, messageId={}, eventType={}",
                event.eventId(), messageId(event), event.eventType(), exception
            );
        }
    }

    private void relayWithTiming(ChatRealtimeEvent event, long submittedAtMs, long submittedNanos) {
        long startedAtMs = System.currentTimeMillis();
        long startedNanos = System.nanoTime();
        try {
            relay(event);
        } finally {
            long finishedNanos = System.nanoTime();
            log.info(
                "채팅 실시간 전송 진단: eventId={}, messageId={}, thread={}, submittedAtMs={}, startedAtMs={},"
                    + " finishedAtMs={}, queueWaitUs={}, relayUs={}, eventType={}",
                event.eventId(), messageId(event), Thread.currentThread().getName(),
                submittedAtMs, startedAtMs, System.currentTimeMillis(),
                (startedNanos - submittedNanos) / 1_000, (finishedNanos - startedNanos) / 1_000, event.eventType()
            );
        }
    }

    private void relay(ChatRealtimeEvent event) {
        try {
            switch (event) {
                case ChatMessageCreatedEvent created -> relayUseCase.relay(created);
                case ChatMessageUpdatedEvent updated -> relayUseCase.relay(updated);
                case ChatMessageDeletedEvent deleted -> relayUseCase.relay(deleted);
                case ChatMessageReactionChangedEvent reactionChanged -> relayUseCase.relay(reactionChanged);
                case ChatReadUpdatedEvent readUpdated -> relayUseCase.relay(readUpdated);
            }
        } catch (RuntimeException exception) {
            log.warn(
                "채팅 실시간 전송 실패: eventId={}, messageId={}, eventType={}",
                event.eventId(), messageId(event), event.eventType(), exception
            );
        }
    }

    private Long messageId(ChatRealtimeEvent event) {
        return switch (event) {
            case ChatMessageCreatedEvent created -> created.messageId();
            case ChatMessageUpdatedEvent updated -> updated.message().messageId();
            case ChatMessageDeletedEvent deleted -> deleted.message().messageId();
            case ChatMessageReactionChangedEvent reactionChanged -> reactionChanged.messageId();
            case ChatReadUpdatedEvent readUpdated -> readUpdated.lastReadMessageId();
        };
    }
}
