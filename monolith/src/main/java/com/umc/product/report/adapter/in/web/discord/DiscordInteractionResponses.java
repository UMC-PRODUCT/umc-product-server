package com.umc.product.report.adapter.in.web.discord;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.umc.product.report.domain.enums.ReportActionReason;

/**
 * Discord 상호작용 응답 본문을 만듭니다.
 */
final class DiscordInteractionResponses {

    static final int PONG = 1;
    static final int CHANNEL_MESSAGE = 4;
    static final int UPDATE_MESSAGE = 7;
    static final int MODAL = 9;
    static final int EPHEMERAL_FLAG = 1 << 6;

    private static final int STRING_SELECT = 3;
    private static final int TEXT_INPUT = 4;
    private static final int LABEL = 18;
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

    /**
     * 사유를 고르는 선택 목록과 메모 입력칸을 담은 입력창입니다. 각 입력은 Discord 입력창의 label 컴포넌트로 감쌉니다.
     */
    static Map<String, Object> reasonModal(
        String customId,
        String title,
        List<ReportActionReason> reasons,
        String reasonInputId,
        String memoInputId,
        int memoMaxLength
    ) {
        List<Map<String, Object>> options = reasons.stream()
            .map(reason -> Map.<String, Object>of("label", DiscordReportReasons.label(reason), "value", reason.name()))
            .toList();
        Map<String, Object> reasonSelect = new LinkedHashMap<>();
        reasonSelect.put("type", STRING_SELECT);
        reasonSelect.put("custom_id", reasonInputId);
        reasonSelect.put("placeholder", "사유를 골라 주세요.");
        reasonSelect.put("min_values", 1);
        reasonSelect.put("max_values", 1);
        reasonSelect.put("required", true);
        reasonSelect.put("options", options);

        Map<String, Object> memoInput = new LinkedHashMap<>();
        memoInput.put("type", TEXT_INPUT);
        memoInput.put("custom_id", memoInputId);
        memoInput.put("style", TEXT_INPUT_PARAGRAPH);
        memoInput.put("max_length", memoMaxLength);
        memoInput.put("required", false);
        memoInput.put("placeholder", "필요하면 판단 근거를 적어 주세요.");

        return Map.of(
            "type", MODAL,
            "data", Map.of(
                "custom_id", customId,
                "title", title,
                "components", List.of(
                    label("사유", null, reasonSelect),
                    label("메모", "기타를 골랐다면 꼭 적어 주세요.", memoInput)
                )
            )
        );
    }

    private static Map<String, Object> label(String text, String description, Map<String, Object> component) {
        Map<String, Object> label = new LinkedHashMap<>();
        label.put("type", LABEL);
        label.put("label", text);
        if (description != null) {
            label.put("description", description);
        }
        label.put("component", component);
        return label;
    }

    static Map<String, Object> updateMessage(List<Map<String, Object>> embeds, List<Map<String, Object>> components) {
        return Map.of(
            "type", UPDATE_MESSAGE,
            "data", Map.of("embeds", embeds, "components", components, "allowed_mentions", NO_MENTIONS)
        );
    }
}
