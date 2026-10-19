package com.umc.product.global.event.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.global.config.OutboxRoutingConfig;
import com.umc.product.global.event.adapter.in.scheduler.EventOutboxPoller;

@ExtendWith(MockitoExtension.class)
@DisplayName("Outbox 그룹별 Worker 실행")
class OutboxWorkerTest {

    @Mock
    private EventOutboxRelayService relay;

    private final OutboxEventRoutingPolicy policy = new OutboxRoutingConfig().outboxEventRoutingPolicy();

    @Test
    @DisplayName("대기 요청을 합치고 executor 실행 전에는 이벤트를 선점하지 않는다")
    void 대기_신호_병합() {
        // Given
        Queue<Runnable> tasks = new ArrayDeque<>();
        var worker = worker(tasks::add);
        // When
        for (int index = 0; index < 100; index++) {
            assertThat(worker.requestRun()).isTrue();
        }
        // Then
        assertThat(tasks).hasSize(1);
        verifyNoInteractions(relay);
        tasks.remove().run();
        verify(relay).relay(policy.selectionFor(OutboxProcessingGroup.REALTIME), 10);
        assertThat(worker.requestRun()).isTrue();
        assertThat(tasks).hasSize(1);
    }

    @Test
    @DisplayName("실행 중 들어온 요청도 한 번의 후속 실행으로 합친다")
    void 실행중_신호_병합() {
        Queue<Runnable> tasks = new ArrayDeque<>();
        var worker = worker(tasks::add);
        doAnswer(invocation -> {
            for (int index = 0; index < 100; index++) {
                worker.requestRun();
            }
            return null;
        }).doAnswer(invocation -> null).when(relay).relay(any(), eq(10));

        worker.requestRun();
        tasks.remove().run();

        verify(relay, times(2)).relay(any(), eq(10));
        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("실행 요청이 거절되어도 다음 요청은 다시 등록할 수 있다")
    void 거절후_재요청() {
        Queue<Runnable> tasks = new ArrayDeque<>();
        Executor executor = new Executor() {
            private boolean reject = true;

            @Override
            public void execute(Runnable task) {
                if (reject) {
                    reject = false;
                    throw new RejectedExecutionException();
                }
                tasks.add(task);
            }
        };
        var worker = worker(executor);

        assertThat(worker.requestRun()).isFalse();
        verifyNoInteractions(relay);
        assertThat(worker.requestRun()).isTrue();
        tasks.remove().run();
        verify(relay).relay(any(), eq(10));
    }

    @Test
    @DisplayName("배치 예외 이후에도 다음 실행 요청을 처리한다")
    void 배치실패후_재실행() {
        Queue<Runnable> tasks = new ArrayDeque<>();
        var worker = worker(tasks::add);
        doThrow(new IllegalStateException("선점 실패")).doNothing().when(relay).relay(any(), eq(10));

        worker.requestRun();
        tasks.remove().run();
        assertThat(worker.requestRun()).isTrue();
        tasks.remove().run();

        verify(relay, times(2)).relay(any(), eq(10));
    }

    @Test
    @DisplayName("종료하면 대기 작업은 선점하지 않고 새 요청도 거절한다")
    void 종료후_선점_방지() {
        Queue<Runnable> tasks = new ArrayDeque<>();
        var worker = worker(tasks::add);
        worker.requestRun();

        worker.close();
        tasks.remove().run();

        assertThat(worker.requestRun()).isFalse();
        verifyNoInteractions(relay);
    }

    @Test
    @DisplayName("백그라운드 배치가 대기해도 독립 executor의 실시간 배치는 진행한다")
    void 그룹별_실행_격리() throws Exception {
        CountDownLatch backgroundStarted = new CountDownLatch(1);
        CountDownLatch releaseBackground = new CountDownLatch(1);
        CountDownLatch realtimeCompleted = new CountDownLatch(1);
        doAnswer(invocation -> {
            backgroundStarted.countDown();
            if (!releaseBackground.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("테스트 대기 시간 초과");
            }
            return null;
        }).when(relay).relay(policy.selectionFor(OutboxProcessingGroup.BACKGROUND), 10);
        doAnswer(invocation -> {
            realtimeCompleted.countDown();
            return null;
        }).when(relay).relay(policy.selectionFor(OutboxProcessingGroup.REALTIME), 10);
        var realtimeExecutor = Executors.newSingleThreadExecutor();
        var backgroundExecutor = Executors.newSingleThreadExecutor();
        var realtime = worker(realtimeExecutor);
        var background = new OutboxWorker(
            OutboxProcessingGroup.BACKGROUND, backgroundExecutor, relay, policy, 10
        );
        var poller = new EventOutboxPoller(realtime, background);
        try {
            poller.pollBackground();
            assertThat(backgroundStarted.await(5, TimeUnit.SECONDS)).isTrue();
            poller.pollRealtime();
            assertThat(realtimeCompleted.await(5, TimeUnit.SECONDS)).isTrue();
        } finally {
            realtime.close();
            background.close();
            releaseBackground.countDown();
            realtimeExecutor.shutdownNow();
            backgroundExecutor.shutdownNow();
            assertThat(realtimeExecutor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
            assertThat(backgroundExecutor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    private OutboxWorker worker(Executor executor) {
        return new OutboxWorker(OutboxProcessingGroup.REALTIME, executor, relay, policy, 10);
    }
}
