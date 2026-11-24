package com.umc.product.community.adapter.in.event;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.umc.product.chat.application.event.ChatMessageCreatedAfterCommitEvent;
import com.umc.product.chat.domain.event.ChatMessageCreatedEvent;
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
    public void onMessageCreated(ChatMessageCreatedAfterCommitEvent event) {
        ChatMessageCreatedEvent messageCreated = event.messageCreated();
        try {
            if (diagnosticsEnabled) {
                long submittedAtMs = System.currentTimeMillis();
                long submittedNanos = System.nanoTime();
                realtimeExecutor.execute(() -> relayWithTiming(messageCreated, submittedAtMs, submittedNanos));
            } else {
                realtimeExecutor.execute(() -> relay(messageCreated));
            }
        } catch (RejectedExecutionException exception) {
            log.warn(
                "채팅 실시간 전송 작업 등록 거절: eventId={}, messageId={}",
                messageCreated.eventId(), messageCreated.messageId(), exception
            );
        }
    }

    private void relayWithTiming(ChatMessageCreatedEvent event, long submittedAtMs, long submittedNanos) {
        long startedAtMs = System.currentTimeMillis();
        long startedNanos = System.nanoTime();
        try {
            relay(event);
        } finally {
            long finishedNanos = System.nanoTime();
            log.info(
                "채팅 실시간 전송 진단: eventId={}, messageId={}, thread={}, submittedAtMs={}, startedAtMs={},"
                    + " finishedAtMs={}, queueWaitUs={}, relayUs={}",
                event.eventId(), event.messageId(), Thread.currentThread().getName(),
                submittedAtMs, startedAtMs, System.currentTimeMillis(),
                (startedNanos - submittedNanos) / 1_000, (finishedNanos - startedNanos) / 1_000
            );
        }
    }

    private void relay(ChatMessageCreatedEvent event) {
        try {
            relayUseCase.relay(event);
        } catch (RuntimeException exception) {
            log.warn("채팅 실시간 전송 실패: eventId={}, messageId={}", event.eventId(), event.messageId(), exception);
        }
    }
}
