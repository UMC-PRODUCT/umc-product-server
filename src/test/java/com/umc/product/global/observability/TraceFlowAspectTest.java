package com.umc.product.global.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TraceFlowAspectTest {

    private Tracer tracer;
    private Span span;
    private Tracer.SpanInScope spanInScope;
    private TraceFlowAspect sut;

    @BeforeEach
    void setUp() {
        tracer = mock(Tracer.class);
        span = mock(Span.class);
        spanInScope = mock(Tracer.SpanInScope.class);

        given(tracer.nextSpan()).willReturn(span);
        given(tracer.withSpan(span)).willReturn(spanInScope);
        given(span.name(org.mockito.ArgumentMatchers.anyString())).willReturn(span);
        given(span.tag(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
            .willReturn(span);
        given(span.start()).willReturn(span);

        sut = new TraceFlowAspect(tracer, new ObservabilityTracingProperties());
    }

    @Test
    @DisplayName("UseCase 구현체 호출을 UseCase 이름의 span으로 감싼다")
    void usecase_구현체_호출_span_생성() throws Throwable {
        Method method = DemoUseCase.class.getMethod("getById", Long.class);
        ProceedingJoinPoint joinPoint = joinPoint(method, new DemoQueryService(), "result");

        Object result = sut.traceUseCaseAndAdapter(joinPoint);

        assertThat(result).isEqualTo("result");
        then(span).should().name("usecase.DemoUseCase.getById");
        then(span).should().tag("app.layer", "application");
        then(span).should().tag("app.domain", "demo");
        then(span).should().tag("app.usecase", "DemoUseCase");
        then(span).should().tag("code.function", "getById");
        then(span).should().end();
        then(spanInScope).should().close();
    }

    @Test
    @DisplayName("adapter.out 호출을 adapter span으로 감싼다")
    void adapter_out_호출_span_생성() throws Throwable {
        Method method = DemoPersistenceAdapter.class.getMethod("getById", Long.class);
        ProceedingJoinPoint joinPoint = joinPoint(method, new DemoPersistenceAdapter(), "entity");

        Object result = sut.traceUseCaseAndAdapter(joinPoint);

        assertThat(result).isEqualTo("entity");
        then(span).should().name("adapter.persistence.DemoPersistenceAdapter.getById");
        then(span).should().tag("app.layer", "adapter.out");
        then(span).should().tag("app.domain", "demo");
        then(span).should().tag("app.adapter.type", "persistence");
        then(span).should().tag("code.function", "getById");
        then(span).should().end();
    }

    @Test
    @DisplayName("동일한 target class와 method의 trace metadata를 캐시한다")
    void 동일_메서드_trace_metadata_캐시() throws Throwable {
        Method method = DemoUseCase.class.getMethod("getById", Long.class);
        DemoQueryService target = new DemoQueryService();

        sut.traceUseCaseAndAdapter(joinPoint(method, target, "first"));
        sut.traceUseCaseAndAdapter(joinPoint(method, target, "second"));

        Field cacheField = TraceFlowAspect.class.getDeclaredField("metadataCache");
        cacheField.setAccessible(true);
        Map<?, ?> metadataCache = (Map<?, ?>) cacheField.get(sut);

        assertThat(metadataCache).hasSize(1);
    }

    private ProceedingJoinPoint joinPoint(Method method, Object target, Object result) throws Throwable {
        MethodSignature signature = mock(MethodSignature.class);
        given(signature.getMethod()).willReturn(method);
        given(signature.getDeclaringType()).willReturn(method.getDeclaringClass());

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        given(joinPoint.getSignature()).willReturn(signature);
        given(joinPoint.getTarget()).willReturn(target);
        given(joinPoint.proceed()).willReturn(result);
        return joinPoint;
    }

    interface DemoUseCase {

        String getById(Long id);
    }

    static class DemoQueryService implements DemoUseCase {

        @Override
        public String getById(Long id) {
            return "result";
        }
    }

    static class DemoPersistenceAdapter {

        public String getById(Long id) {
            return "entity";
        }
    }
}
