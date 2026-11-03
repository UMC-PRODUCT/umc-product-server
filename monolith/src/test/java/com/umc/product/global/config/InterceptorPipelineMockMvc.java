package com.umc.product.global.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.umc.product.demoday.adapter.in.web.support.CurrentDemodayParticipantArgumentResolver;
import com.umc.product.global.client.ClientContextProperties;
import com.umc.product.global.client.ClientOriginRegistry;
import com.umc.product.global.client.ClientRequestClassifier;
import com.umc.product.global.logging.OperationalMetrics;
import com.umc.product.global.ratelimit.ApiRateLimitInterceptor;
import com.umc.product.global.security.resolver.CurrentMemberArgumentResolver;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 운영과 같은 {@code @EnableWebMvc} 설정으로 WebMvcConfigurer 들을 조립한 MockMvc 를 만든다.
 *
 * <p>{@code standaloneSetup().addInterceptors(...)} 는 registry 가 만든 MappedInterceptor 를 한 번 더 감싸
 * path pattern 을 무시하므로, 인터셉터 순서와 include/exclude 경로를 검증할 때는 이 조립을 쓴다.
 */
public final class InterceptorPipelineMockMvc {

    public static final String TRACE_ID = "test-trace-id";

    private InterceptorPipelineMockMvc() {
    }

    /**
     * configurer 는 주어진 순서대로 bean 으로 등록되고, Spring 도 이 순서로 {@code addInterceptors} 를 호출한다.
     */
    public static MockMvc build(List<WebMvcConfigurer> configurers, Object... controllers) {
        GenericWebApplicationContext context = new GenericWebApplicationContext(new MockServletContext());
        AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
        context.registerBean(MvcConfig.class);
        for (int index = 0; index < configurers.size(); index++) {
            WebMvcConfigurer configurer = configurers.get(index);
            context.registerBean("configurer" + index, WebMvcConfigurer.class, () -> configurer);
        }
        for (int index = 0; index < controllers.length; index++) {
            registerController(context, "controller" + index, controllers[index]);
        }
        context.refresh();
        return MockMvcBuilders.webAppContextSetup(context)
            .addFilters(new TraceIdMdcFilter())
            .build();
    }

    public static WebMvcConfig webMvcConfig(ApiRateLimitInterceptor apiRateLimitInterceptor) {
        Map<String, Object> rateLimitBeans = apiRateLimitInterceptor == null
            ? Map.of()
            : Map.of("apiRateLimitInterceptor", apiRateLimitInterceptor);
        return new WebMvcConfig(
            new CurrentMemberArgumentResolver(null),
            new StaticListableBeanFactory().getBeanProvider(CurrentDemodayParticipantArgumentResolver.class),
            new TraceIdHeaderInterceptor(),
            loggingInterceptor(),
            new StaticListableBeanFactory(rateLimitBeans).getBeanProvider(ApiRateLimitInterceptor.class)
        );
    }

    public static LoggingInterceptor loggingInterceptor() {
        return new LoggingInterceptor(
            new ClientRequestClassifier(new ClientOriginRegistry(new ClientContextProperties(List.of()))),
            new OperationalMetrics(new SimpleMeterRegistry())
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> void registerController(GenericWebApplicationContext context, String name, T controller) {
        context.registerBean(name, (Class<T>) controller.getClass(), () -> controller);
    }

    /**
     * {@link LoggingInterceptor} 가 남긴 요청 로그의 event 이름을 모은다.
     */
    public static final class RequestLogCapture implements AutoCloseable {

        private final Logger logger = (Logger) LoggerFactory.getLogger(LoggingInterceptor.class);
        private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
        private final Level previousLevel;

        public RequestLogCapture() {
            previousLevel = logger.getLevel();
            logger.setLevel(Level.INFO);
            appender.start();
            logger.addAppender(appender);
        }

        public List<String> events() {
            return new ArrayList<>(appender.list).stream()
                .map(event -> event.getMessage().split(" ", 2)[0])
                .toList();
        }

        @Override
        public void close() {
            logger.detachAppender(appender);
            logger.setLevel(previousLevel);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    static class MvcConfig {
    }

    // 운영에서 Micrometer Tracing 이 하듯 요청을 처리하는 동안만 MDC 에 traceId 를 둔다.
    private static final class TraceIdMdcFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
            MDC.put("traceId", TRACE_ID);
            try {
                chain.doFilter(request, response);
            } finally {
                MDC.remove("traceId");
            }
        }
    }
}
