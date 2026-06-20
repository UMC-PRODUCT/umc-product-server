package com.umc.product.global.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.micrometer.context.ContextRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.contextpropagation.ObservationAwareSpanThreadLocalAccessor;
import io.micrometer.tracing.test.simple.SimpleTracer;

/**
 * 비동기 경계(@Async 스레드풀)에서 trace 컨텍스트가 끊기지 않는지 검증한다.
 *
 * <p>{@link SpanContextPropagationConfig} 가 등록하는 {@link ObservationAwareSpanThreadLocalAccessor} 와
 * {@link ContextPropagatingTaskDecorator} 의 조합으로, 직접 생성한 Span 의 traceId 가 작업 스레드로
 * 전파됨을 {@link SimpleTracer} 로 확인한다.
 */
class SpanContextPropagationTest {

    private SimpleTracer tracer;

    @BeforeEach
    void setUp() {
        tracer = new SimpleTracer();
        // SpanContextPropagationConfig 와 동일하게 Span 전용 accessor 를 전역 ContextRegistry 에 등록한다.
        ContextRegistry.getInstance()
            .registerThreadLocalAccessor(new ObservationAwareSpanThreadLocalAccessor(ObservationRegistry.NOOP, tracer));
    }

    @AfterEach
    void tearDown() {
        ContextRegistry.getInstance().removeThreadLocalAccessor(ObservationAwareSpanThreadLocalAccessor.KEY);
    }

    @Test
    @DisplayName("TaskDecorator 가 장착된 Executor 는 비동기 스레드로 넘어가도 같은 traceId 를 전파한다")
    void TaskDecorator_장착_시_traceId_전파() throws InterruptedException {
        // given: 데코레이터가 장착된 Executor 와, 현재 스레드에 시작된 Span
        ThreadPoolTaskExecutor executor = newExecutor(new ContextPropagatingTaskDecorator());
        Span span = tracer.nextSpan().name("root").start();
        AtomicReference<String> propagatedTraceId = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        // when: Span 이 scope 에 있는 동안 작업을 제출하고, 작업 스레드에서 현재 traceId 를 읽는다
        String expectedTraceId;
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            expectedTraceId = tracer.currentSpan().context().traceId();
            executor.execute(() -> {
                Span current = tracer.currentSpan();
                if (current != null) {
                    propagatedTraceId.set(current.context().traceId());
                }
                latch.countDown();
            });
            latch.await(2, TimeUnit.SECONDS);
        } finally {
            span.end();
            executor.shutdown();
        }

        // then: 작업 스레드가 원 요청과 동일한 traceId 를 본다
        assertThat(propagatedTraceId.get())
            .isNotNull()
            .isEqualTo(expectedTraceId);
    }

    @Test
    @DisplayName("TaskDecorator 가 없는 Executor 는 비동기 스레드로 trace 컨텍스트가 전파되지 않는다")
    void TaskDecorator_미장착_시_traceId_단절() throws InterruptedException {
        // given: 데코레이터가 없는 Executor 와, 현재 스레드에 시작된 Span
        ThreadPoolTaskExecutor executor = newExecutor(null);
        Span span = tracer.nextSpan().name("root").start();
        AtomicReference<String> propagatedTraceId = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        // when: Span 이 scope 에 있는 동안 작업을 제출한다
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            executor.execute(() -> {
                Span current = tracer.currentSpan();
                if (current != null) {
                    propagatedTraceId.set(current.context().traceId());
                }
                latch.countDown();
            });
            latch.await(2, TimeUnit.SECONDS);
        } finally {
            span.end();
            executor.shutdown();
        }

        // then: 작업 스레드에는 현재 Span 이 없어 traceId 가 단절된다 (데코레이터의 효과 대조)
        assertThat(propagatedTraceId.get()).isNull();
    }

    private ThreadPoolTaskExecutor newExecutor(ContextPropagatingTaskDecorator taskDecorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1);
        executor.setThreadNamePrefix("test-async-");
        if (taskDecorator != null) {
            executor.setTaskDecorator(taskDecorator);
        }
        executor.initialize();
        return executor;
    }
}
