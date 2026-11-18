package com.umc.product.community.adapter.in.event;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.springframework.beans.factory.annotation.Qualifier;
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

    public CommunityChatMessageAfterCommitListener(
        RelayCommunityThreadRealtimeEventUseCase relayUseCase,
        @Qualifier("communityChatRealtimeExecutor") Executor realtimeExecutor
    ) {
        this.relayUseCase = relayUseCase;
        this.realtimeExecutor = realtimeExecutor;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessageCreated(ChatMessageCreatedAfterCommitEvent event) {
        ChatMessageCreatedEvent messageCreated = event.messageCreated();
        try {
            realtimeExecutor.execute(() -> relay(messageCreated));
        } catch (RejectedExecutionException exception) {
            log.warn(
                "채팅 실시간 전송 작업 등록 거절: eventId={}, messageId={}",
                messageCreated.eventId(), messageCreated.messageId(), exception
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
