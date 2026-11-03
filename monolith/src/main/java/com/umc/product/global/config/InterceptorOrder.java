package com.umc.product.global.config;

/**
 * MVC 인터셉터 실행 순서. Spring 은 등록한 configurer 나 위치와 관계없이 이 값으로 정렬한다.
 *
 * <p>trace id 헤더를 가장 먼저 붙여 rate limit 차단 응답에도 {@code X-Trace-Id} 가 남게 한다.
 * rate limit 은 로깅보다 먼저 둬서 차단된 요청이 요청 시작/완료 로그를 남기지 않게 한다.
 */
public final class InterceptorOrder {

    public static final int TRACE_ID_HEADER = 100;
    public static final int RATE_LIMIT = 200;
    public static final int LOGGING = 300;

    private InterceptorOrder() {
    }
}
