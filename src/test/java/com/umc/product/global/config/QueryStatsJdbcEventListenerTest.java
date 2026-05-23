package com.umc.product.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.p6spy.engine.common.PreparedStatementInformation;
import com.umc.product.global.observability.ObservabilityTracingProperties;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QueryStatsJdbcEventListenerTest {

    private Tracer tracer;
    private Span span;
    private Tracer.SpanInScope spanInScope;
    private QueryStatsJdbcEventListener sut;

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

        sut = new QueryStatsJdbcEventListener(tracer, new ObservabilityTracingProperties());
    }

    @AfterEach
    void tearDown() {
        QueryStatsHolder.clear();
    }

    @Test
    @DisplayName("DB 쿼리 실행을 child span으로 남기고 요청 단위 쿼리 통계를 기록한다")
    void db_쿼리_span_및_요청_쿼리_통계_기록() {
        PreparedStatementInformation info = mock(PreparedStatementInformation.class);
        given(info.getSql()).willReturn("select * from member where id = ?");

        QueryStatsHolder.init();

        sut.onBeforeExecuteQuery(info);
        sut.onAfterExecuteQuery(info, 12_500_000L, null);

        assertThat(QueryStatsHolder.getQueryCount()).isEqualTo(1L);
        assertThat(QueryStatsHolder.getTotalTimeMs()).isEqualTo(12L);
        then(span).should().name("db.query");
        then(span).should().tag("db.system", "postgresql");
        then(span).should().tag("db.operation", "SELECT");
        then(span).should().tag("db.query.elapsed_ms", "12");
        then(span).should().end();
        then(spanInScope).should().close();
    }

    @Test
    @DisplayName("DB 쿼리 실패 시 span에 예외를 기록하고 요청 통계에는 성공 쿼리만 반영한다")
    void db_쿼리_실패_span_error_기록() {
        PreparedStatementInformation info = mock(PreparedStatementInformation.class);
        SQLException exception = new SQLException("boom");

        QueryStatsHolder.init();

        sut.onBeforeExecuteQuery(info);
        sut.onAfterExecuteQuery(info, 3_000_000L, exception);

        assertThat(QueryStatsHolder.getQueryCount()).isZero();
        then(span).should().error(exception);
        then(span).should().tag("db.query.elapsed_ms", "3");
        then(span).should().end();
        then(spanInScope).should().close();
    }
}
