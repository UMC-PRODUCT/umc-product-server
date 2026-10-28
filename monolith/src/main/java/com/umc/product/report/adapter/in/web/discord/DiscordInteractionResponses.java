package com.umc.product.report.adapter.in.web.discord;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Discord 상호작용 응답 본문을 만듭니다.
 */
final class DiscordInteractionResponses {

    static final int PONG = 1;
    static final int CHANNEL_MESSAGE = 4;
    static final int UPDATE_MESSAGE = 7;
    static final int MODAL = 9;
    static final int EPHEMERAL_FLAG = 1 << 6;

    private static final int ACTION_ROW = 1;
    private static final int TEXT_INPUT = 4;
    private static final int TEXT_INPUT_PARAGRAPH = 2;
    private static final Map<String, Object> NO_MENTIONS = Map.of("parse", List.of());

    private DiscordInteractionResponses() {
    }

    static Map<String, Object> pong() {
        return Map.of("type", PONG);
    }

    /**
     * 누른 사람에게만 보이는 안내 메시지입니다.
     */
    static Map<String, Object> ephemeral(String content) {
        return Map.of(
            "type", CHANNEL_MESSAGE,
            "data", Map.of("content", content, "flags", EPHEMERAL_FLAG, "allowed_mentions", NO_MENTIONS)
        );
    }

    static Map<String, Object> reasonModal(String customId, String title, String inputId, int maxLength) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("type", TEXT_INPUT);
        input.put("custom_id", inputId);
        input.put("label", "사유");
        input.put("style", TEXT_INPUT_PARAGRAPH);
        input.put("min_length", 1);
        input.put("max_length", maxLength);
        input.put("required", true);
        input.put("placeholder", "판단 근거를 적어 주세요.");
        return Map.of(
            "type", MODAL,
            "data", Map.of(
                "custom_id", customId,
                "title", title,
                "components", List.of(Map.of("type", ACTION_ROW, "components", List.of(input)))
            )
        );
    }

    static Map<String, Object> updateMessage(List<Map<String, Object>> embeds, List<Map<String, Object>> components) {
        return Map.of(
            "type", UPDATE_MESSAGE,
            "data", Map.of("embeds", embeds, "components", components, "allowed_mentions", NO_MENTIONS)
        );
    }
}
