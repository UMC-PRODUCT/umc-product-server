package com.umc.product.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

/**
 * ADR-016 의 LoggingInterceptor MDC 라이프사이클을 검증한다.
 *
 * <p>Spring Context 를 띄우지 않는 순수 단위 테스트로, preHandle 시점의 MDC put
 * 과 afterCompletion 시점의 finally MDC.clear 가 보장되는지 확인한다.
 *
 * <p>JSON 어펜더가 실제로 직렬화하는 MDC 스냅샷은 ILoggingEvent.getMDCPropertyMap() 로
 * 캡처해서 검증한다 (실제 운영에서는 logstash-logback-encoder 가 같은 map 을 사용).
 */
class LoggingInterceptorTest {

    private final LoggingInterceptor interceptor = new LoggingInterceptor();

    private Logger interceptorLogger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        interceptorLogger = (Logger) LoggerFactory.getLogger(LoggingInterceptor.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        interceptorLogger.addAppender(listAppender);
        interceptorLogger.setLevel(Level.INFO);
    }

    @AfterEach
    void tearDown() {
        interceptorLogger.detachAppender(listAppender);
        // 테스트 간 MDC 누수 방지
        MDC.clear();
    }

    @Test
    @DisplayName("preHandle 시 requestId / method / path 가 MDC 에 들어가고 X-Request-Id 헤더가 채워진다")
    void preHandle_MDC_등록_성공() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/forms/123/answers");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertThat(result).isTrue();
        assertThat(MDC.get("requestId")).isNotBlank();
        assertThat(MDC.get("method")).isEqualTo("GET");
        assertThat(MDC.get("path")).isEqualTo("/forms/123/answers");
        assertThat(response.getHeader("X-Request-Id")).isEqualTo(MDC.get("requestId"));
    }

    @Test
    @DisplayName("afterCompletion 의 finally 에서 MDC 가 반드시 비워진다")
    void afterCompletion_MDC_누수_방지() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/forms/123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        interceptor.preHandle(request, response, new Object());

        // pre-condition: MDC 에 값이 들어있다
        assertThat(MDC.get("requestId")).isNotBlank();

        // when
        interceptor.afterCompletion(request, response, new Object(), null);

        // then
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("api_request_completed 로그의 MDC 스냅샷에 uriTemplate / statusCode / durationMs 가 포함된다")
    void afterCompletion_응답_필드_MDC_등록() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/forms/123/answers");
        request.setAttribute(
            HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE,
            "/forms/{formId}/answers"
        );
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        interceptor.preHandle(request, response, new Object());

        // when
        interceptor.afterCompletion(request, response, new Object(), null);

        // then — 로그 이벤트의 MDC 스냅샷을 캡처해서 확인
        Map<String, String> snapshot = findEventMdc("api_request_completed");
        assertThat(snapshot).isNotNull();
        assertThat(snapshot).containsEntry("event", "api_request_completed");
        assertThat(snapshot).containsEntry("uriTemplate", "/forms/{formId}/answers");
        assertThat(snapshot).containsEntry("statusCode", "200");
        assertThat(snapshot).containsKey("durationMs");
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("preHandle 이 호출되지 않은 상태에서 afterCompletion 이 호출되어도 안전하게 종료된다")
    void afterCompletion_startTime_없을_때_안전_종료() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/forms/123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when / then — 예외 없이 통과
        interceptor.afterCompletion(request, response, new Object(), null);
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("X-Forwarded-For 헤더가 있으면 첫 번째 IP 가 clientIp 로 채워진다")
    void clientIp_X_Forwarded_For_우선() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/forms");
        request.addHeader("X-Forwarded-For", "203.0.113.7, 10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        interceptor.preHandle(request, response, new Object());

        // when
        interceptor.afterCompletion(request, response, new Object(), null);

        // then
        Map<String, String> snapshot = findEventMdc("api_request_completed");
        assertThat(snapshot).isNotNull();
        assertThat(snapshot).containsEntry("clientIp", "203.0.113.7");
    }

    private Map<String, String> findEventMdc(String message) {
        List<ILoggingEvent> events = listAppender.list;
        for (ILoggingEvent event : events) {
            if (message.equals(event.getMessage())) {
                return event.getMDCPropertyMap();
            }
        }
        return null;
    }
}
