package com.umc.product.report.adapter.discord;

/**
 * Discord에 보내거나 저장할 문자열을 다룹니다.
 * <p>
 * 이모지처럼 두 개의 char로 이루어진 문자를 중간에서 자르면 깨진 문자가 남고, Discord가 요청을 거절할 수 있습니다. 그래서 길이를 char가 아니라
 * 문자(code point) 단위로 셉니다.
 */
public final class DiscordText {

    private DiscordText() {
    }

    public static int length(String value) {
        return value.codePointCount(0, value.length());
    }

    /**
     * 문자 단위로 {@code maxLength}까지만 남깁니다. 잘랐으면 {@code suffix}를 붙입니다.
     */
    public static String truncate(String value, int maxLength, String suffix) {
        if (length(value) <= maxLength) {
            return value;
        }
        return value.substring(0, value.offsetByCodePoints(0, maxLength)) + suffix;
    }
}
