package com.umc.product.figma.adapter.in.web.support;

/**
 * Discord webhook URL 의 ID / token 마지막 4자만 노출하고 나머지는 별표로 가린다. ADR-005 마스킹 정책에 따라 응답 매핑 시점에 적용한다.
 * <p>
 * 예: {@code https://discord.com/api/webhooks/123456789012345678/abcdef...xyz123} →
 * {@code https://discord.com/api/webhooks/****5678/****z123}
 */
public final class DiscordWebhookUrlMasker {

    private static final String MASK = "****";
    private static final int VISIBLE_TAIL = 4;

    private DiscordWebhookUrlMasker() {
    }

    public static String mask(String webhookUrl) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return webhookUrl;
        }
        int lastSlash = webhookUrl.lastIndexOf('/');
        if (lastSlash < 0) {
            return MASK;
        }
        String token = webhookUrl.substring(lastSlash + 1);
        String beforeToken = webhookUrl.substring(0, lastSlash);

        int prevSlash = beforeToken.lastIndexOf('/');
        if (prevSlash < 0) {
            return beforeToken + "/" + maskTail(token);
        }
        String id = beforeToken.substring(prevSlash + 1);
        String prefix = beforeToken.substring(0, prevSlash);
        return prefix + "/" + maskTail(id) + "/" + maskTail(token);
    }

    private static String maskTail(String value) {
        if (value.length() <= VISIBLE_TAIL) {
            return MASK;
        }
        return MASK + value.substring(value.length() - VISIBLE_TAIL);
    }
}
