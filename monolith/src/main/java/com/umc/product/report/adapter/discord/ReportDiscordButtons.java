package com.umc.product.report.adapter.discord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.umc.product.report.domain.enums.ReportActionType;

/**
 * 신고 메시지에 붙는 조치 버튼의 형식입니다.
 * <p>
 * 메시지를 보내는 어댑터와 버튼 클릭을 받는 어댑터가 서로를 참조하지 않고 같은 형식을 쓰도록 이곳에 둡니다. 버튼 ID는
 * {@code report:{신고 ID}:{조치}} 형식입니다.
 */
public final class ReportDiscordButtons {

    private static final String PREFIX = "report";
    private static final String SEPARATOR = ":";
    private static final int ACTION_ROW = 1;
    private static final int BUTTON = 2;
    private static final int STYLE_SECONDARY = 2;
    private static final int STYLE_DANGER = 4;

    private ReportDiscordButtons() {
    }

    /**
     * 조치 버튼을 담은 Discord 컴포넌트 목록을 만듭니다. 처리가 끝난 메시지에는 {@code disabled}로 비활성 버튼을 붙입니다.
     */
    public static List<Map<String, Object>> actionRows(Long reportId, boolean disabled) {
        List<Map<String, Object>> buttons = new ArrayList<>();
        for (ReportActionType actionType : ReportActionType.values()) {
            buttons.add(Map.of(
                "type", BUTTON,
                "style", style(actionType),
                "label", label(actionType),
                "custom_id", customId(reportId, actionType),
                "disabled", disabled
            ));
        }
        return List.of(Map.of("type", ACTION_ROW, "components", buttons));
    }

    public static String customId(Long reportId, ReportActionType actionType) {
        return PREFIX + SEPARATOR + reportId + SEPARATOR + actionKey(actionType);
    }

    /**
     * 버튼 ID에서 신고 ID와 조치를 읽습니다. 형식이 다르면 비어 있는 값을 돌려줍니다.
     */
    public static Optional<Target> parse(String customId) {
        if (customId == null) {
            return Optional.empty();
        }
        String[] parts = customId.split(SEPARATOR, -1);
        if (parts.length != 3 || !PREFIX.equals(parts[0])) {
            return Optional.empty();
        }
        long reportId;
        try {
            reportId = Long.parseLong(parts[1]);
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
        if (reportId <= 0) {
            return Optional.empty();
        }
        for (ReportActionType actionType : ReportActionType.values()) {
            if (actionKey(actionType).equals(parts[2])) {
                return Optional.of(new Target(reportId, actionType));
            }
        }
        return Optional.empty();
    }

    /**
     * 버튼과 처리 결과에 보여 줄 조치 이름입니다.
     */
    public static String label(ReportActionType actionType) {
        return switch (actionType) {
            case APPROVE -> "처리 완료";
            case REJECT -> "반려";
        };
    }

    private static String actionKey(ReportActionType actionType) {
        return switch (actionType) {
            case APPROVE -> "approve";
            case REJECT -> "reject";
        };
    }

    private static int style(ReportActionType actionType) {
        return switch (actionType) {
            case APPROVE -> STYLE_DANGER;
            case REJECT -> STYLE_SECONDARY;
        };
    }

    public record Target(Long reportId, ReportActionType actionType) {
    }
}
