package com.umc.product.global.event.application.service;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import com.umc.product.global.event.application.port.out.OutboxEventSelection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class OutboxWorker implements AutoCloseable {

    private final OutboxProcessingGroup group;
    private final Executor executor;
    private final EventOutboxRelayService relay;
    private final OutboxEventSelection selection;
    private final int batchSize;
    private boolean scheduled;
    private boolean pending;
    private boolean closed;

    public OutboxWorker(
        OutboxProcessingGroup group,
        Executor executor,
        EventOutboxRelayService relay,
        OutboxEventRoutingPolicy routingPolicy,
        int batchSize
    ) {
        this.group = Objects.requireNonNull(group);
        this.executor = Objects.requireNonNull(executor);
        this.relay = Objects.requireNonNull(relay);
        this.selection = routingPolicy.selectionFor(group);
        if (batchSize <= 0) {
            throw new IllegalArgumentException("배치 크기는 양수여야 합니다.");
        }
        this.batchSize = batchSize;
    }

    // 대기 중이거나 실행 중인 요청은 하나의 후속 배치 신호로 합친다.
    public synchronized boolean requestRun() {
        if (closed) {
            return false;
        }
        pending = true;
        if (scheduled) {
            return true;
        }
        scheduled = true;
        try {
            executor.execute(this::drain);
            return true;
        } catch (RejectedExecutionException exception) {
            scheduled = false;
            pending = false;
            log.warn("Outbox worker 실행 요청 거절: group={}", group.name());
            return false;
        }
    }

    private void drain() {
        try {
            while (takeSignal()) {
                try {
                    relay.relay(selection, batchSize);
                } catch (RuntimeException exception) {
                    log.error("Outbox worker 배치 실행 실패: group={}", group.name(), exception);
                }
            }
        } catch (Error error) {
            synchronized (this) {
                scheduled = false;
                pending = false;
            }
            throw error;
        }
    }

    private synchronized boolean takeSignal() {
        if (closed || !pending) {
            scheduled = false;
            return false;
        }
        pending = false;
        return true;
    }

    // 실행 중인 배치는 완료하도록 두고 추가 선점은 중지한다.
    @Override
    public synchronized void close() {
        closed = true;
        pending = false;
    }
}
