package com.umc.product.global.event.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
        verify(relay).relay(OutboxProcessingGroup.REALTIME, policy.selectionFor(OutboxProcessingGroup.REALTIME), 10);
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
            return 0;
        }).doAnswer(invocation -> 0).when(relay).relay(any(), any(), eq(10));

        worker.requestRun();
        tasks.remove().run();

        verify(relay, times(2)).relay(any(), any(), eq(10));
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
        verify(relay).relay(any(), any(), eq(10));
    }

    @Test
    @DisplayName("배치 예외 이후에도 다음 실행 요청을 처리한다")
    void 배치실패후_재실행() {
        Queue<Runnable> tasks = new ArrayDeque<>();
        var worker = worker(tasks::add);
        when(relay.relay(any(), any(), eq(10))).thenThrow(new IllegalStateException("선점 실패")).thenReturn(0);

        worker.requestRun();
        tasks.remove().run();
        assertThat(worker.requestRun()).isTrue();
        tasks.remove().run();

        verify(relay, times(2)).relay(any(), any(), eq(10));
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
            return 0;
        }).when(relay).relay(
            OutboxProcessingGroup.BACKGROUND, policy.selectionFor(OutboxProcessingGroup.BACKGROUND), 10
        );
        doAnswer(invocation -> {
            realtimeCompleted.countDown();
            return 0;
        }).when(relay).relay(OutboxProcessingGroup.REALTIME, policy.selectionFor(OutboxProcessingGroup.REALTIME), 10);
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

    @Test
    @DisplayName("동시 실행 수만큼 배치를 병렬 처리하고 반복 요청에도 상한을 지킨다")
    void 병렬_실행_상한과_신호_병합() throws Exception {
        CountDownLatch started = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger active = new AtomicInteger();
        AtomicInteger maximum = new AtomicInteger();
        doAnswer(invocation -> {
            int current = active.incrementAndGet();
            maximum.accumulateAndGet(current, Math::max);
            started.countDown();
            try {
                if (!release.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("테스트 대기 시간 초과");
                }
            } finally {
                active.decrementAndGet();
            }
            return 0;
        }).when(relay).relay(any(), any(), eq(10));
        var executor = Executors.newFixedThreadPool(4);
        var worker = new OutboxWorker(OutboxProcessingGroup.REALTIME, executor, relay, policy,
            10, EventOutboxRelayMetrics.noOp(), 2);
        try {
            assertThat(worker.requestRun()).isTrue();
            assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
            for (int index = 0; index < 100; index++) {
                worker.requestRun();
            }
            release.countDown();
            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
            verify(relay, times(4)).relay(any(), any(), eq(10));
            assertThat(maximum.get()).isEqualTo(2);
        } finally {
            worker.close();
            release.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    @DisplayName("슬롯 일부가 거절되어도 승인된 슬롯을 유지하고 다음 요청으로 복구한다")
    void 부분_거절_복구() {
        Queue<Runnable> tasks = new ArrayDeque<>();
        AtomicInteger submitted = new AtomicInteger();
        Executor executor = task -> {
            if (submitted.incrementAndGet() == 2) {
                throw new RejectedExecutionException();
            }
            tasks.add(task);
        };
        var worker = new OutboxWorker(OutboxProcessingGroup.REALTIME, executor, relay, policy,
            10, EventOutboxRelayMetrics.noOp(), 2);

        assertThat(worker.requestRun()).isTrue();
        assertThat(tasks).hasSize(1);
        assertThat(worker.requestRun()).isTrue();
        assertThat(tasks).hasSize(2);
        tasks.remove().run();
        tasks.remove().run();

        verify(relay, times(2)).relay(any(), any(), eq(10));
    }

    @Test
    @DisplayName("병렬 슬롯의 배치 예외는 다른 슬롯과 후속 실행을 막지 않는다")
    void 병렬_슬롯_실패_격리() {
        Queue<Runnable> tasks = new ArrayDeque<>();
        var worker = new OutboxWorker(OutboxProcessingGroup.REALTIME, tasks::add, relay, policy,
            10, EventOutboxRelayMetrics.noOp(), 2);
        when(relay.relay(any(), any(), eq(10))).thenThrow(new IllegalStateException("선점 실패")).thenReturn(0);
        worker.requestRun();
        tasks.remove().run();
        tasks.remove().run();
        worker.requestRun();
        worker.close();
        tasks.remove().run();
        tasks.remove().run();

        verify(relay, times(2)).relay(any(), any(), eq(10));
        assertThat(worker.requestRun()).isFalse();
    }

    @Test
    @DisplayName("한 번의 신호로 실시간 적체를 여러 배치 처리하고 부분 배치에서 멈춘다")
    void 여러_배치_연속_처리() {
        // given
        Queue<Runnable> tasks = new ArrayDeque<>();
        var worker = worker(tasks::add);
        when(relay.relay(any(), any(), eq(10))).thenReturn(10, 10, 3);

        // when
        worker.requestRun();
        assertThat(tasks).hasSize(1);
        tasks.remove().run();

        // then
        verify(relay, times(3)).relay(any(), any(), eq(10));
        assertThat(tasks).isEmpty();
        assertThat(worker.requestRun()).isTrue();
        assertThat(tasks).hasSize(1);
    }

    @Test
    @DisplayName("배치 크기의 정확한 배수를 처리하면 빈 조회 한 번 이후 멈춘다")
    void 정확한_배수_후_빈_조회() {
        // given
        Queue<Runnable> tasks = new ArrayDeque<>();
        var worker = worker(tasks::add);
        when(relay.relay(any(), any(), eq(10))).thenReturn(10, 10, 0);

        // when
        worker.requestRun();
        tasks.remove().run();

        // then
        verify(relay, times(3)).relay(any(), any(), eq(10));
        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("가득 찬 배치 도중 들어온 신호는 다음 연속 배치에 합친다")
    void 연속_배치와_대기_신호_병합() {
        // given
        Queue<Runnable> tasks = new ArrayDeque<>();
        var worker = worker(tasks::add);
        doAnswer(invocation -> {
            for (int index = 0; index < 100; index++) {
                worker.requestRun();
            }
            return 10;
        }).doReturn(3).when(relay).relay(any(), any(), eq(10));

        // when
        worker.requestRun();
        tasks.remove().run();

        // then
        verify(relay, times(2)).relay(any(), any(), eq(10));
        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("가득 찬 배치 실행 중 종료하면 추가 배치를 선점하지 않는다")
    void 연속_배치_종료() {
        // given
        Queue<Runnable> tasks = new ArrayDeque<>();
        var worker = worker(tasks::add);
        doAnswer(invocation -> {
            worker.close();
            return 10;
        }).when(relay).relay(any(), any(), eq(10));

        // when
        worker.requestRun();
        tasks.remove().run();

        // then
        verify(relay).relay(any(), any(), eq(10));
        assertThat(worker.requestRun()).isFalse();
        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("백그라운드는 가득 찬 배치여도 추가 신호 없이 연속 처리하지 않는다")
    void 백그라운드_한_배치_유지() {
        // given
        Queue<Runnable> tasks = new ArrayDeque<>();
        var worker = new OutboxWorker(OutboxProcessingGroup.BACKGROUND, tasks::add, relay, policy, 10);
        when(relay.relay(any(), any(), eq(10))).thenReturn(10);

        // when
        worker.requestRun();
        tasks.remove().run();

        // then
        verify(relay).relay(any(), any(), eq(10));
        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("연속 배치가 실패하면 자동 반복을 멈추고 다음 요청으로 복구한다")
    void 연속_배치_실패_후_복구() {
        // given
        Queue<Runnable> tasks = new ArrayDeque<>();
        var worker = worker(tasks::add);
        when(relay.relay(any(), any(), eq(10))).thenReturn(10)
            .thenThrow(new IllegalStateException("선점 실패")).thenReturn(0);

        // when
        worker.requestRun();
        tasks.remove().run();
        verify(relay, times(2)).relay(any(), any(), eq(10));

        // then
        assertThat(worker.requestRun()).isTrue();
        tasks.remove().run();
        verify(relay, times(3)).relay(any(), any(), eq(10));
        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("연속 배치와 반복 신호에도 동시성 상한과 executor 제출 수를 유지한다")
    void 연속_배치_동시성_상한() throws Exception {
        // given
        CountDownLatch started = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger batches = new AtomicInteger(5);
        AtomicInteger active = new AtomicInteger();
        AtomicInteger maximum = new AtomicInteger();
        AtomicInteger submitted = new AtomicInteger();
        doAnswer(invocation -> {
            int current = active.incrementAndGet();
            maximum.accumulateAndGet(current, Math::max);
            int claimed = batches.getAndUpdate(count -> Math.max(0, count - 1)) > 0 ? 10 : 0;
            started.countDown();
            try {
                if (!release.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("테스트 대기 시간 초과");
                }
                return claimed;
            } finally {
                active.decrementAndGet();
            }
        }).when(relay).relay(any(), any(), eq(10));
        var executor = Executors.newFixedThreadPool(4);
        Executor countingExecutor = task -> {
            submitted.incrementAndGet();
            executor.execute(task);
        };
        var worker = new OutboxWorker(OutboxProcessingGroup.REALTIME, countingExecutor, relay, policy,
            10, EventOutboxRelayMetrics.noOp(), 2);

        // when
        try {
            worker.requestRun();
            assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
            for (int index = 0; index < 100; index++) {
                worker.requestRun();
            }
            release.countDown();
            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

            // then
            verify(relay, times(7)).relay(any(), any(), eq(10));
            assertThat(maximum.get()).isEqualTo(2);
            assertThat(submitted.get()).isEqualTo(2);
        } finally {
            worker.close();
            release.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    @DisplayName("동시 실행 수가 양수가 아니면 구성을 거부한다")
    void 잘못된_동시성_거부() {
        assertThatIllegalArgumentException().isThrownBy(() -> new OutboxWorker(
            OutboxProcessingGroup.REALTIME, Runnable::run, relay, policy,
            10, EventOutboxRelayMetrics.noOp(), 0
        ));
    }

    private OutboxWorker worker(Executor executor) {
        return new OutboxWorker(OutboxProcessingGroup.REALTIME, executor, relay, policy, 10);
    }
}
