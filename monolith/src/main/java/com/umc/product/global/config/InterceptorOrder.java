package com.umc.product.global.config;

/**
 * MVC 인터셉터 실행 순서. Spring 은 등록한 configurer 나 위치와 관계없이 이 값으로 정렬한다.
 *
 * <p>trace id 헤더를 가장 먼저 붙여 rate limit 차단 응답에도 {@code X-Trace-Id} 가 남게 한다.
 * rate limit 은 로깅보다 먼저 둬서 차단된 요청이 요청 시작/완료 로그를 남기지 않게 한다.
 *
 * <p>{@code .order()} 없이 등록한 인터셉터는 Spring 기본값 0 을 받는다. {@link #RATE_LIMIT} 을 0 으로 둔 것은
 * 순서를 빠뜨린 limiter 도 trace id 뒤, 로깅 앞에서 돌게 하기 위해서다. Spring 이 직접 붙이는
 * {@code ConversionServiceExposingInterceptor}, {@code ResourceUrlProviderExposingInterceptor} 도 0 이다.
 * 값이 같은 인터셉터끼리는 등록 순서를 따르고, configurer 등록 순서는 패키지 이름 순이다.
 */
public final class InterceptorOrder {

    public static final int TRACE_ID_HEADER = -100;
    public static final int RATE_LIMIT = 0;
    public static final int LOGGING = 100;

    private InterceptorOrder() {
    }
}
