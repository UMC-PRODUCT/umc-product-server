package com.umc.product.global.client;

import java.util.Locale;

public enum ClientDeviceType {
    IOS,
    ANDROID,
    DESKTOP,
    UNKNOWN;

    /**
     * User-Agent 문자열만으로 기기를 판정한다. 헤더 판정 규칙은 이 함수 한 곳에만 두고,
     * {@link ClientRequestClassifier}와 웹 페이지 컨트롤러가 함께 사용한다.
     */
    public static ClientDeviceType fromUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return UNKNOWN;
        }
        String normalizedUserAgent = userAgent.toLowerCase(Locale.ROOT);
        if (normalizedUserAgent.contains("iphone") || normalizedUserAgent.contains("ipad")) {
            return IOS;
        }
        if (normalizedUserAgent.contains("android")) {
            return ANDROID;
        }
        return DESKTOP;
    }
}
