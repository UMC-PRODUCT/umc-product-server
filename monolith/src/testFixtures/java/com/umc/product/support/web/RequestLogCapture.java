package com.umc.product.support.web;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.LoggerFactory;

import com.umc.product.global.config.LoggingInterceptor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * {@link LoggingInterceptor} 가 남긴 요청 로그의 MDC 를 모은다.
 *
 * <p>Loki 와 대시보드는 메시지가 아니라 MDC {@code event} 필드로 요청 로그를 집계하므로, 같은 필드를 읽는다.
 */
public final class RequestLogCapture implements AutoCloseable {

    private final Logger logger = (Logger) LoggerFactory.getLogger(LoggingInterceptor.class);
    private final List<Map<String, String>> mdcSnapshots = new CopyOnWriteArrayList<>();
    private final AppenderBase<ILoggingEvent> appender = new AppenderBase<>() {
        @Override
        protected void append(ILoggingEvent event) {
            // LoggingInterceptor 는 로그 직후 event 키를 지우므로 기록 시점의 MDC 를 복사해 둔다.
            mdcSnapshots.add(Map.copyOf(event.getMDCPropertyMap()));
        }
    };
    private final Level previousLevel;

    public RequestLogCapture() {
        previousLevel = logger.getLevel();
        logger.setLevel(Level.INFO);
        appender.start();
        logger.addAppender(appender);
    }

    public List<String> events() {
        return mdcValues("event");
    }

    public List<String> statusCodes() {
        return mdcValues("statusCode");
    }

    private List<String> mdcValues(String key) {
        return mdcSnapshots.stream()
            .filter(snapshot -> snapshot.containsKey(key))
            .map(snapshot -> snapshot.get(key))
            .toList();
    }

    @Override
    public void close() {
        logger.detachAppender(appender);
        appender.stop();
        logger.setLevel(previousLevel);
    }
}
