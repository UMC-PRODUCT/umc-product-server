package com.umc.product.report.adapter.in.web.discord;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.report.adapter.discord.DiscordText;
import com.umc.product.report.adapter.discord.ReportDiscordButtons;
import com.umc.product.report.application.port.in.command.ModerateReportUseCase;
import com.umc.product.report.application.port.in.command.dto.ModerateReportCommand;
import com.umc.product.report.application.port.in.command.dto.ModerateReportResult;
import com.umc.product.report.domain.ReportAction;
import com.umc.product.report.domain.enums.ReportActionReason;
import com.umc.product.report.domain.enums.ReportActionType;
import com.umc.product.report.domain.exception.ReportDomainException;
import com.umc.product.report.domain.exception.ReportErrorCode;

import lombok.extern.slf4j.Slf4j;

/**
 * 서명 검증을 통과한 Discord 상호작용을 처리합니다.
 * <ol>
 *     <li>버튼 클릭(MESSAGE_COMPONENT): 서버와 신고처리 역할을 확인하고 사유 입력창(MODAL)을 띄웁니다. 아직 아무것도 바꾸지 않습니다.</li>
 *     <li>사유 제출(MODAL_SUBMIT): 권한과 사유·메모를 다시 확인하고 조치를 적용한 뒤, 원래 메시지에 결과를 붙이고 버튼을 비활성으로
 *     바꿉니다.</li>
 * </ol>
 * 권한이 없거나 이미 처리된 신고면 누른 사람에게만 보이는 안내로 응답합니다. Discord는 3초 안에 응답을 받아야 하므로 모든 처리를 동기로 끝냅니다.
 */
@Slf4j
@Component
public class DiscordInteractionHandler {

    static final int TYPE_PING = 1;
    static final int TYPE_MESSAGE_COMPONENT = 3;
    static final int TYPE_MODAL_SUBMIT = 5;
    static final String REASON_INPUT_ID = "reason";
    static final String MEMO_INPUT_ID = "memo";
    static final int MODERATOR_NAME_MAX_LENGTH = 100;

    static final String MESSAGE_UNKNOWN_BUTTON = "알 수 없는 버튼이에요.";
    static final String MESSAGE_NO_PERMISSION = "신고를 처리할 권한이 없어요.";
    static final String MESSAGE_REASON_REQUIRED = "사유를 골라 주세요.";
    static final String MESSAGE_MEMO_REQUIRED = "기타를 골랐다면 메모를 입력해 주세요.";
    static final String MESSAGE_MEMO_TOO_LONG = "메모는 " + ReportAction.MEMO_MAX_LENGTH + "자까지 입력할 수 있어요.";
    static final String MESSAGE_ALREADY_MODERATED = "이미 다른 운영자가 처리한 신고예요.";
    static final String MESSAGE_NOT_FOUND = "신고를 찾을 수 없어요.";
    static final String MESSAGE_APPLIED_WITHOUT_UPDATE = "처리는 완료됐지만 메시지를 갱신하지 못했어요. 버튼은 더 이상 동작하지 않아요.";
    static final String MESSAGE_FAILED = "신고를 처리하지 못했어요. 잠시 후 다시 시도해 주세요.";

    private static final String RESULT_FIELD_NAME = "처리 결과";
    private static final int COLOR_APPROVED = 0xED4245;
    private static final int COLOR_REJECTED = 0x95A5A6;
    private static final TypeReference<LinkedHashMap<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ModerateReportUseCase moderateReportUseCase;
    private final ReportDiscordInteractionProperties properties;
    private final ObjectMapper objectMapper;

    public DiscordInteractionHandler(
        ModerateReportUseCase moderateReportUseCase,
        ReportDiscordInteractionProperties properties,
        ObjectMapper objectMapper
    ) {
        this.moderateReportUseCase = moderateReportUseCase;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 처리하지 않는 상호작용 유형이면 비어 있는 값을 돌려줍니다.
     */
    public Optional<Map<String, Object>> handle(JsonNode interaction) {
        return switch (interaction.path("type").asInt()) {
            case TYPE_PING -> Optional.of(DiscordInteractionResponses.pong());
            case TYPE_MESSAGE_COMPONENT -> Optional.of(handleButton(interaction));
            case TYPE_MODAL_SUBMIT -> Optional.of(handleReasonSubmit(interaction));
            default -> Optional.empty();
        };
    }

    private Map<String, Object> handleButton(JsonNode interaction) {
        String customId = interaction.path("data").path("custom_id").asText(null);
        Optional<ReportDiscordButtons.Target> target = ReportDiscordButtons.parse(customId);
        if (target.isEmpty()) {
            return DiscordInteractionResponses.ephemeral(MESSAGE_UNKNOWN_BUTTON);
        }
        if (!isModerator(interaction)) {
            return DiscordInteractionResponses.ephemeral(MESSAGE_NO_PERMISSION);
        }
        ReportDiscordButtons.Target button = target.get();
        String title = "신고 #" + button.reportId() + " · " + ReportDiscordButtons.label(button.actionType());
        return DiscordInteractionResponses.reasonModal(
            customId,
            title,
            DiscordReportReasons.of(button.actionType()),
            REASON_INPUT_ID,
            MEMO_INPUT_ID,
            ReportAction.MEMO_MAX_LENGTH
        );
    }

    private Map<String, Object> handleReasonSubmit(JsonNode interaction) {
        Optional<ReportDiscordButtons.Target> target =
            ReportDiscordButtons.parse(interaction.path("data").path("custom_id").asText(null));
        if (target.isEmpty()) {
            return DiscordInteractionResponses.ephemeral(MESSAGE_UNKNOWN_BUTTON);
        }
        // 사유 제출은 버튼 클릭과 별개의 요청이므로 권한을 다시 확인합니다.
        if (!isModerator(interaction)) {
            return DiscordInteractionResponses.ephemeral(MESSAGE_NO_PERMISSION);
        }
        JsonNode inputs = interaction.path("data").path("components");
        // 다른 조치의 사유가 들어오면(버튼 ID를 바꾼 요청 등) 고르지 않은 것으로 봅니다.
        Optional<ReportActionReason> reason = findInput(inputs, REASON_INPUT_ID)
            .map(input -> input.path("values").path(0).asText(null))
            .flatMap(DiscordReportReasons::parse)
            .filter(code -> code.actionType() == target.get().actionType());
        if (reason.isEmpty()) {
            return DiscordInteractionResponses.ephemeral(MESSAGE_REASON_REQUIRED);
        }
        String memo = findInput(inputs, MEMO_INPUT_ID)
            .map(input -> input.path("value").asText(null))
            .filter(value -> !value.isBlank())
            .orElse(null);
        if (memo == null && reason.get().isMemoRequired()) {
            return DiscordInteractionResponses.ephemeral(MESSAGE_MEMO_REQUIRED);
        }
        if (memo != null && DiscordText.length(memo.trim()) > ReportAction.MEMO_MAX_LENGTH) {
            return DiscordInteractionResponses.ephemeral(MESSAGE_MEMO_TOO_LONG);
        }

        JsonNode member = interaction.path("member");
        ModerateReportCommand command = new ModerateReportCommand(
            target.get().reportId(),
            target.get().actionType(),
            reason.get(),
            memo,
            member.path("user").path("id").asText(),
            displayName(member)
        );
        ModerateReportResult result;
        try {
            result = moderateReportUseCase.moderate(command);
        } catch (ReportDomainException exception) {
            return DiscordInteractionResponses.ephemeral(messageFor(exception));
        } catch (RuntimeException exception) {
            log.error("Discord 신고 조치 처리 실패: reportId={}", target.get().reportId(), exception);
            return DiscordInteractionResponses.ephemeral(MESSAGE_FAILED);
        }
        // 조치는 이미 커밋되었으므로, 메시지 조립에 실패해도 실패로 안내하지 않고 결과만 알립니다.
        try {
            return resolvedMessage(interaction, result);
        } catch (RuntimeException exception) {
            log.error("Discord 신고 처리 결과 메시지 조립 실패: reportId={}", result.reportId(), exception);
            return DiscordInteractionResponses.ephemeral(MESSAGE_APPLIED_WITHOUT_UPDATE);
        }
    }

    /**
     * 설정한 서버에서 신고처리 역할을 가진 사람인지 확인합니다. 서버 ID나 역할 ID가 설정되지 않았으면 아무도 처리할 수 없습니다.
     */
    private boolean isModerator(JsonNode interaction) {
        if (!properties.hasModeratorScope()) {
            return false;
        }
        if (!properties.guildId().equals(interaction.path("guild_id").asText(null))) {
            return false;
        }
        for (JsonNode role : interaction.path("member").path("roles")) {
            if (properties.moderatorRoleId().equals(role.asText())) {
                return true;
            }
        }
        return false;
    }

    private static String displayName(JsonNode member) {
        String name = firstText(
            member.path("nick"),
            member.path("user").path("global_name"),
            member.path("user").path("username")
        );
        if (name == null) {
            return member.path("user").path("id").asText();
        }
        return DiscordText.truncate(name, MODERATOR_NAME_MAX_LENGTH, "");
    }

    private static String firstText(JsonNode... candidates) {
        for (JsonNode candidate : candidates) {
            if (candidate.isTextual() && !candidate.asText().isBlank()) {
                return candidate.asText();
            }
        }
        return null;
    }

    /**
     * 입력창 구조(행 안의 components 배열, 또는 label 안의 component)와 상관없이 {@code customId} 입력을 찾습니다.
     */
    private static Optional<JsonNode> findInput(JsonNode node, String customId) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return Optional.empty();
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                Optional<JsonNode> found = findInput(child, customId);
                if (found.isPresent()) {
                    return found;
                }
            }
            return Optional.empty();
        }
        if (customId.equals(node.path("custom_id").asText(null))) {
            return Optional.of(node);
        }
        Optional<JsonNode> found = findInput(node.path("components"), customId);
        return found.isPresent() ? found : findInput(node.path("component"), customId);
    }

    /**
     * 원래 메시지의 제목과 항목은 그대로 두고 처리 결과를 붙인 뒤, 버튼을 비활성으로 바꿉니다.
     */
    private Map<String, Object> resolvedMessage(JsonNode interaction, ModerateReportResult result) {
        JsonNode originalEmbed = interaction.path("message").path("embeds").path(0);
        Map<String, Object> embed = new LinkedHashMap<>();
        embed.put("title", originalEmbed.path("title").asText("신고 #" + result.reportId()));

        List<Object> fields = new ArrayList<>();
        if (originalEmbed.path("fields").isArray()) {
            for (JsonNode field : originalEmbed.path("fields")) {
                fields.add(objectMapper.convertValue(field, MAP_TYPE));
            }
        }
        fields.add(Map.of("name", RESULT_FIELD_NAME, "value", resultText(result), "inline", false));
        embed.put("fields", fields);
        embed.put("color", result.actionType() == ReportActionType.APPROVE ? COLOR_APPROVED : COLOR_REJECTED);

        return DiscordInteractionResponses.updateMessage(
            List.of(embed),
            ReportDiscordButtons.actionRows(result.reportId(), true)
        );
    }

    static String resultText(ModerateReportResult result) {
        String text = ReportDiscordButtons.label(result.actionType())
            + " by " + result.moderatorName()
            + " · " + DiscordReportReasons.label(result.reasonCode());
        if (result.memo() == null) {
            return text;
        }
        return text + "\n메모: " + result.memo();
    }

    private static String messageFor(ReportDomainException exception) {
        if (exception.getBaseCode() == ReportErrorCode.REPORT_ALREADY_MODERATED) {
            return MESSAGE_ALREADY_MODERATED;
        }
        if (exception.getBaseCode() == ReportErrorCode.REPORT_NOT_FOUND) {
            return MESSAGE_NOT_FOUND;
        }
        return MESSAGE_FAILED;
    }
}
