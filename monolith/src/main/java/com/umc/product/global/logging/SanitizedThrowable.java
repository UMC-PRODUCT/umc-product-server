package com.umc.product.global.logging;

import java.util.IdentityHashMap;
import java.util.Map;

import com.umc.product.global.observability.ObservabilityErrorSanitizer;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;

/**
 * 메시지만 정제한 예외 사본이다. 프레임, 원인, suppressed, 순환 구조는 원본과 같다.
 *
 * <p>OTLP 어펜더는 {@code ThrowableProxy}가 감싼 {@link Throwable}에서만 스택을 꺼내고, {@code IThrowableProxy}를
 * 직접 구현한 객체는 스택 없이 버린다. 그래서 정제는 프록시가 아니라 예외 자체를 복제해서 한다.
 *
 * <p>OTLP 의 {@code exception.type}과 logback 의 {@code IThrowableProxy.getClassName()}은 이 클래스명이 된다.
 * 원래 클래스명은 {@link #toString()}에 남긴다. 그래서 {@code toString()}을 쓰는 렌더러(OTLP 의
 * {@code exception.stacktrace}, logback 의 기본 스택 출력)의 첫 줄과 {@code Caused by:} 줄에만 원래 이름이 나온다.
 * logback 의 순환 표기 줄은 {@code toString()}을 쓰지 않아 이 클래스명이 나온다.
 */
final class SanitizedThrowable extends RuntimeException {

    private final String rendered;

    private SanitizedThrowable(String message, String rendered) {
        super(message);
        this.rendered = rendered;
    }

    /**
     * 원본 예외가 없는 프록시(네트워크로 받은 {@code ThrowableProxyVO} 등)는 사본을 만들 수 없으므로 버린다.
     * 이 앱의 이벤트는 모두 로거에서 바로 오므로 항상 {@link ThrowableProxy}를 갖는다.
     */
    static IThrowableProxy proxyOf(IThrowableProxy proxy) {
        if (!(proxy instanceof ThrowableProxy original)) {
            return null;
        }
        return new ThrowableProxy(copyOf(original.getThrowable(), new IdentityHashMap<>()));
    }

    private static Throwable copyOf(Throwable original, Map<Throwable, Throwable> copies) {
        if (original == null) {
            return null;
        }
        Throwable existing = copies.get(original);
        if (existing != null) {
            return existing;
        }

        SanitizedThrowable copy = new SanitizedThrowable(
            ObservabilityErrorSanitizer.sanitizeMessage(original.getMessage()),
            ObservabilityErrorSanitizer.sanitizeMessage(original.toString())
        );
        // 순환 원인이 같은 사본을 가리키도록 자식을 복제하기 전에 등록한다.
        copies.put(original, copy);
        copy.setStackTrace(original.getStackTrace());

        Throwable cause = copyOf(original.getCause(), copies);
        // getCause()를 재정의해 자기 자신을 돌려주는 예외는 initCause 가 거부하므로 원인 없이 둔다.
        if (cause != null && cause != copy) {
            copy.initCause(cause);
        }
        for (Throwable suppressed : original.getSuppressed()) {
            copy.addSuppressed(copyOf(suppressed, copies));
        }
        return copy;
    }

    /**
     * 프레임은 원본 것으로 덮어쓰므로 사본을 만든 위치의 스택은 수집하지 않는다.
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    @Override
    public String toString() {
        return rendered;
    }
}
