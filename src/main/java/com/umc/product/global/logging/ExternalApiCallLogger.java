package com.umc.product.global.logging;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 외부 API 호출(예: GitHub / Apple / Kakao / LLM provider / 이메일)을 통일된
 * 구조화 이벤트({@code external_api_called})로 기록하기 위한 헬퍼다.
 *
 * <p>본 헬퍼는 hook 만 제공한다. 도메인별 적용 (Apple / Kakao / GitHub / LLM …)은 ADR-016
 * 의 범위 밖에서 별도 PR 로 점진적으로 추가한다.
 *
 * <h2>호출 예시</h2>
 * <pre>{@code
 * GithubPullRequest pr = ExternalApiCallLogger.measure(
 *     "GITHUB",
 *     "FETCH_PULL_REQUESTS",
 *     () -> githubClient.fetchPullRequests(repo)
 * );
 * }</pre>
 *
 * <h2>출력되는 JSON 필드</h2>
 * <ul>
 *     <li>{@code provider} — 외부 서비스 식별자 (예: {@code GITHUB}, {@code OPENAI})</li>
 *     <li>{@code operation} — 호출 동작 (예: {@code FETCH_PULL_REQUESTS})</li>
 *     <li>{@code result} — {@code SUCCESS} / {@code FAILURE}</li>
 *     <li>{@code durationMs} — 호출 소요 시간</li>
 *     <li>{@code errorClass} — 실패 시 예외의 단순 클래스명 (메시지는 의도적으로 제외)</li>
 * </ul>
 *
 * <h2>민감정보 처리</h2>
 * 예외 메시지를 그대로 로깅하지 않는다. 메시지에 OAuth code / access token / 사용자 입력이
 * 섞일 수 있어 ADR-016 §민감 필드 정책을 위반할 수 있다. 메시지가 꼭 필요한 경우 호출자가
 * 직접 마스킹한 뒤 별도 로그 라인으로 남긴다.
 */
public final class ExternalApiCallLogger {

    /** 외부 API 호출 전용 logger. JSON 라인의 {@code logger} 필드로 식별된다. */
    private static final Logger log = LoggerFactory.getLogger("external_api");

    private static final String EVENT = "external_api_called";

    private ExternalApiCallLogger() {
        // util
    }

    /**
     * 외부 API 호출을 감싸 성공/실패와 소요 시간을 구조화 이벤트로 남긴다.
     *
     * <p>예외는 그대로 재던지므로 호출자의 흐름이 변하지 않는다.
     *
     * @param provider  외부 서비스 식별자 (예: {@code GITHUB}, {@code APPLE})
     * @param operation 호출 동작 (예: {@code FETCH_PULL_REQUESTS}, {@code EXCHANGE_TOKEN})
     * @param call      실제 호출 람다
     * @param <T>       호출 결과 타입
     * @return {@code call} 의 반환값
     */
    public static <T> T measure(String provider, String operation, Supplier<T> call) {
        // 경과 시간 측정은 monotonic clock 인 nanoTime 을 사용한다.
        // currentTimeMillis 는 NTP 동기화 등 wall-clock 보정 시 뒤로 점프할 수 있어 durationMs 가 왜곡된다.
        long startNanos = System.nanoTime();
        try {
            T result = call.get();
            log.info(
                EVENT,
                kv("provider", provider),
                kv("operation", operation),
                kv("result", "SUCCESS"),
                kv("durationMs", (System.nanoTime() - startNanos) / 1_000_000L)
            );
            return result;
        } catch (RuntimeException e) {
            log.warn(
                EVENT,
                kv("provider", provider),
                kv("operation", operation),
                kv("result", "FAILURE"),
                kv("durationMs", (System.nanoTime() - startNanos) / 1_000_000L),
                kv("errorClass", e.getClass().getSimpleName())
            );
            throw e;
        }
    }

    /**
     * 반환값이 없는 외부 API 호출용 오버로드. {@link #measure(String, String, Supplier)} 와 동일한
     * 이벤트 스키마를 사용한다.
     */
    public static void measure(String provider, String operation, Runnable call) {
        measure(provider, operation, () -> {
            call.run();
            return null;
        });
    }
}
