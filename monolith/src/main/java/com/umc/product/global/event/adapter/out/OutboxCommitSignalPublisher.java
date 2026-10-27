package com.umc.product.global.event.adapter.out;

import java.util.Collection;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.umc.product.global.event.application.event.RealtimeOutboxCommittedEvent;
import com.umc.product.global.event.application.port.in.ResolveOutboxProcessingGroupUseCase;
import com.umc.product.global.event.domain.EventOutbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxCommitSignalPublisher {

    private final ResolveOutboxProcessingGroupUseCase routingPolicy;
    private final ApplicationEventPublisher eventPublisher;

    public void requestAfterCommit(Collection<EventOutbox> outboxes) {
        boolean realtime = outboxes.stream()
            .anyMatch(outbox -> "realtime".equals(routingPolicy.resolveProcessingGroup(outbox.getEventType())));
        if (!realtime) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()
            || !TransactionSynchronizationManager.isActualTransactionActive()) {
            // 커밋 경계를 관찰할 수 없으면 조기 실행 대신 복구 폴링에 맡긴다.
            return;
        }
        // 동기화 목록은 REQUIRES_NEW의 suspend/resume 경계를 따르므로 트랜잭션별로 신호를 합친다.
        boolean registered = TransactionSynchronizationManager.getSynchronizations().stream()
            .anyMatch(CommitSignalSynchronization.class::isInstance);
        if (!registered) {
            TransactionSynchronizationManager.registerSynchronization(new CommitSignalSynchronization());
        }
    }

    private void publishSignal() {
        try {
            eventPublisher.publishEvent(new RealtimeOutboxCommittedEvent());
        } catch (RuntimeException exception) {
            // 저장된 이벤트는 복구 폴링이 처리한다. 신호 실패를 이미 커밋된 요청의 실패로 전파하지 않는다.
            log.warn("실시간 Outbox 실행 신호 발행 실패. 복구 폴링에서 재시도합니다.");
        }
    }

    private final class CommitSignalSynchronization implements TransactionSynchronization {

        @Override
        public void afterCommit() {
            publishSignal();
        }
    }
}
