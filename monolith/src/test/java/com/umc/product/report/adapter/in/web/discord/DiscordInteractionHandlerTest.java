package com.umc.product.report.adapter.in.web.discord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.report.application.port.in.command.ModerateReportUseCase;
import com.umc.product.report.application.port.in.command.dto.ModerateReportCommand;
import com.umc.product.report.application.port.in.command.dto.ModerateReportResult;
import com.umc.product.report.domain.enums.ReportActionReason;
import com.umc.product.report.domain.enums.ReportActionType;
import com.umc.product.report.domain.enums.ReportStatus;
import com.umc.product.report.domain.enums.ReportTargetRemoval;
import com.umc.product.report.domain.exception.ReportDomainException;
import com.umc.product.report.domain.exception.ReportErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("Discord 상호작용 처리")
class DiscordInteractionHandlerTest {

    private static final String GUILD_ID = "100";
    private static final String MODERATOR_ROLE_ID = "200";
    private static final String APPROVE_BUTTON = "report:901:approve";
    private static final String REJECT_BUTTON = "report:901:reject";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    ModerateReportUseCase moderateReportUseCase;

    DiscordInteractionHandler sut;

    @BeforeEach
    void setUp() {
        sut = new DiscordInteractionHandler(
            moderateReportUseCase,
            new ReportDiscordInteractionProperties("unused", GUILD_ID, MODERATOR_ROLE_ID),
            objectMapper
        );
    }

    @Test
    @DisplayName("PING이면 PONG으로 응답한다")
    void handle_PING() throws Exception {
        assertThat(sut.handle(json("{\"type\":1}"))).contains(Map.of("type", 1));
    }

    @Test
    @DisplayName("처리하지 않는 유형이면 비어 있는 값을 돌려준다")
    void handle_처리하지_않는_유형() throws Exception {
        assertThat(sut.handle(json("{\"type\":2}"))).isEmpty();
    }

    @Test
    @DisplayName("신고처리 역할이 버튼을 누르면 아무것도 바꾸지 않고 그 조치의 사유 목록과 메모 입력창을 띄운다")
    void 버튼_클릭_권한이_있으면_입력창() throws Exception {
        // when
        JsonNode response = respond(buttonClick(APPROVE_BUTTON, GUILD_ID, MODERATOR_ROLE_ID));

        // then
        assertThat(response.path("type").asInt()).isEqualTo(DiscordInteractionResponses.MODAL);
        assertThat(response.at("/data/custom_id").asText()).isEqualTo(APPROVE_BUTTON);
        assertThat(response.at("/data/title").asText()).isEqualTo("신고 #901 · 처리 완료");

        JsonNode reasonSelect = response.at("/data/components/0/component");
        assertThat(reasonSelect.path("custom_id").asText()).isEqualTo(DiscordInteractionHandler.REASON_INPUT_ID);
        assertThat(reasonSelect.path("required").asBoolean()).isTrue();
        List<String> values = new ArrayList<>();
        reasonSelect.path("options").forEach(option -> values.add(option.path("value").asText()));
        assertThat(values).containsExactly("SPAM", "ABUSE", "INAPPROPRIATE", "PRIVACY", "OTHER_VIOLATION");
        assertThat(reasonSelect.at("/options/1/label").asText()).isEqualTo("욕설·비방·혐오 표현");

        JsonNode memoInput = response.at("/data/components/1/component");
        assertThat(memoInput.path("custom_id").asText()).isEqualTo(DiscordInteractionHandler.MEMO_INPUT_ID);
        assertThat(memoInput.path("max_length").asInt()).isEqualTo(500);
        assertThat(memoInput.path("required").asBoolean()).isFalse();
        then(moderateReportUseCase).should(never()).moderate(any());
    }

    @Test
    @DisplayName("반려 버튼이면 반려 사유 목록을 띄운다")
    void 반려_버튼은_반려_사유를_띄운다() throws Exception {
        // when
        JsonNode response = respond(buttonClick(REJECT_BUTTON, GUILD_ID, MODERATOR_ROLE_ID));

        // then
        List<String> labels = new ArrayList<>();
        response.at("/data/components/0/component/options")
            .forEach(option -> labels.add(option.path("label").asText()));
        assertThat(labels).containsExactly(
            "운영 정책 위반 아님",
            "신고 내용과 확인 결과가 일치하지 않음",
            "신고 내용 확인 어려움",
            "중복 신고 또는 이미 처리된 신고",
            "기타"
        );
    }

    @Test
    @DisplayName("신고처리 역할이 없거나 다른 서버에서 누르면 본인에게만 안내한다")
    void 버튼_클릭_권한이_없으면_안내() throws Exception {
        assertEphemeral(respond(buttonClick(APPROVE_BUTTON, GUILD_ID, "999")),
            DiscordInteractionHandler.MESSAGE_NO_PERMISSION);
        assertEphemeral(respond(buttonClick(APPROVE_BUTTON, "other-guild", MODERATOR_ROLE_ID)),
            DiscordInteractionHandler.MESSAGE_NO_PERMISSION);
    }

    @Test
    @DisplayName("서버 ID나 역할 ID가 설정되지 않았으면 아무도 처리할 수 없다")
    void 설정이_없으면_아무도_처리할_수_없다() throws Exception {
        // given
        DiscordInteractionHandler unconfigured = new DiscordInteractionHandler(
            moderateReportUseCase,
            new ReportDiscordInteractionProperties("unused", null, null),
            objectMapper
        );

        // when
        Map<String, Object> response = unconfigured.handle(buttonClick(APPROVE_BUTTON, GUILD_ID, MODERATOR_ROLE_ID))
            .orElseThrow();

        // then
        assertEphemeral(objectMapper.valueToTree(response), DiscordInteractionHandler.MESSAGE_NO_PERMISSION);
    }

    @Test
    @DisplayName("형식이 다른 버튼이면 본인에게만 안내한다")
    void 알_수_없는_버튼이면_안내() throws Exception {
        assertEphemeral(respond(buttonClick("other:1", GUILD_ID, MODERATOR_ROLE_ID)),
            DiscordInteractionHandler.MESSAGE_UNKNOWN_BUTTON);
    }

    @Test
    @DisplayName("사유를 제출하면 조치를 적용하고 결과를 붙인 메시지로 바꾸며 버튼을 비활성으로 만든다")
    void 사유_제출_조치_적용() throws Exception {
        // given
        given(moderateReportUseCase.moderate(any())).willReturn(approvedResult("욕설 확인"));

        // when
        JsonNode response = respond(reasonSubmit(APPROVE_BUTTON, MODERATOR_ROLE_ID, "ABUSE", "  욕설 확인  "));

        // then
        ArgumentCaptor<ModerateReportCommand> captor = ArgumentCaptor.forClass(ModerateReportCommand.class);
        then(moderateReportUseCase).should().moderate(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new ModerateReportCommand(
            901L, ReportActionType.APPROVE, ReportActionReason.ABUSE, "  욕설 확인  ", "300", "모루"
        ));

        assertThat(response.path("type").asInt()).isEqualTo(DiscordInteractionResponses.UPDATE_MESSAGE);
        JsonNode embed = response.at("/data/embeds/0");
        assertThat(embed.path("title").asText()).isEqualTo("신고 접수 #901");
        assertThat(embed.at("/fields/0/name").asText()).isEqualTo("신고 ID");
        assertThat(embed.at("/fields/1/name").asText()).isEqualTo("처리 결과");
        assertThat(embed.at("/fields/1/value").asText()).isEqualTo("처리 완료 by 모루 · 욕설·비방·혐오 표현\n메모: 욕설 확인");
        List<JsonNode> buttons = List.of(
            response.at("/data/components/0/components/0"),
            response.at("/data/components/0/components/1")
        );
        assertThat(buttons).allSatisfy(button -> assertThat(button.path("disabled").asBoolean()).isTrue());
        assertThat(response.at("/data/allowed_mentions/parse").isEmpty()).isTrue();
    }

    @Test
    @DisplayName("이미 처리된 신고면 본인에게만 안내한다")
    void 사유_제출_이미_처리된_신고() throws Exception {
        // given
        given(moderateReportUseCase.moderate(any()))
            .willThrow(new ReportDomainException(ReportErrorCode.REPORT_ALREADY_MODERATED));

        // when & then
        assertEphemeral(respond(reasonSubmit(APPROVE_BUTTON, MODERATOR_ROLE_ID, "ABUSE", null)),
            DiscordInteractionHandler.MESSAGE_ALREADY_MODERATED);
    }

    @Test
    @DisplayName("사유 제출도 권한을 다시 확인한다")
    void 사유_제출_권한_재확인() throws Exception {
        assertEphemeral(respond(reasonSubmit(APPROVE_BUTTON, "999", "ABUSE", null)),
            DiscordInteractionHandler.MESSAGE_NO_PERMISSION);
        then(moderateReportUseCase).should(never()).moderate(any());
    }

    @Test
    @DisplayName("사유를 고르지 않았거나 다른 조치의 사유를 보내면 처리하지 않고 안내한다")
    void 사유_검증() throws Exception {
        assertEphemeral(respond(reasonSubmit(APPROVE_BUTTON, MODERATOR_ROLE_ID, null, "메모")),
            DiscordInteractionHandler.MESSAGE_REASON_REQUIRED);
        assertEphemeral(respond(reasonSubmit(APPROVE_BUTTON, MODERATOR_ROLE_ID, "UNKNOWN", null)),
            DiscordInteractionHandler.MESSAGE_REASON_REQUIRED);
        assertEphemeral(respond(reasonSubmit(APPROVE_BUTTON, MODERATOR_ROLE_ID, "NOT_VIOLATION", null)),
            DiscordInteractionHandler.MESSAGE_REASON_REQUIRED);
        then(moderateReportUseCase).should(never()).moderate(any());
    }

    @Test
    @DisplayName("기타 사유에 메모가 없거나 메모가 500자를 넘으면 처리하지 않고 안내한다")
    void 메모_검증() throws Exception {
        assertEphemeral(respond(reasonSubmit(REJECT_BUTTON, MODERATOR_ROLE_ID, "OTHER_REJECTION", "   ")),
            DiscordInteractionHandler.MESSAGE_MEMO_REQUIRED);
        assertEphemeral(respond(reasonSubmit(APPROVE_BUTTON, MODERATOR_ROLE_ID, "SPAM", "가".repeat(501))),
            DiscordInteractionHandler.MESSAGE_MEMO_TOO_LONG);
        then(moderateReportUseCase).should(never()).moderate(any());
    }

    @Test
    @DisplayName("메모 없이 사유만 골라도 처리하고, 결과에는 사유만 남긴다")
    void 메모_없이_처리() throws Exception {
        // given
        given(moderateReportUseCase.moderate(any())).willReturn(new ModerateReportResult(
            901L, ReportStatus.REJECTED, ReportActionType.REJECT, ReportActionReason.DUPLICATE, null,
            ReportTargetRemoval.NONE, "모루", Instant.now()
        ));

        // when
        JsonNode response = respond(reasonSubmit(REJECT_BUTTON, MODERATOR_ROLE_ID, "DUPLICATE", null));

        // then
        ArgumentCaptor<ModerateReportCommand> captor = ArgumentCaptor.forClass(ModerateReportCommand.class);
        then(moderateReportUseCase).should().moderate(captor.capture());
        assertThat(captor.getValue().reasonCode()).isEqualTo(ReportActionReason.DUPLICATE);
        assertThat(captor.getValue().memo()).isNull();
        assertThat(response.at("/data/embeds/0/fields/1/value").asText())
            .isEqualTo("반려 by 모루 · 중복 신고 또는 이미 처리된 신고");
    }

    @Test
    @DisplayName("메모 길이는 문자 단위로 세서 이모지 500개도 받아들인다")
    void 메모_길이는_문자_단위로_센다() throws Exception {
        // given
        given(moderateReportUseCase.moderate(any())).willReturn(approvedResult("😀".repeat(500)));

        // when
        JsonNode response = respond(reasonSubmit(APPROVE_BUTTON, MODERATOR_ROLE_ID, "SPAM", "😀".repeat(500)));

        // then
        assertThat(response.path("type").asInt()).isEqualTo(DiscordInteractionResponses.UPDATE_MESSAGE);
    }

    @Test
    @DisplayName("처리 중 예상하지 못한 오류가 나면 본인에게만 안내한다")
    void 사유_제출_예상하지_못한_오류() throws Exception {
        // given
        given(moderateReportUseCase.moderate(any())).willThrow(new IllegalStateException("DB 오류"));

        // when & then
        assertEphemeral(respond(reasonSubmit(APPROVE_BUTTON, MODERATOR_ROLE_ID, "ABUSE", null)),
            DiscordInteractionHandler.MESSAGE_FAILED);
    }

    @Test
    @DisplayName("조치를 적용한 뒤 결과 메시지 조립에 실패하면 실패가 아니라 처리 완료로 본인에게 안내한다")
    void 사유_제출_조치_후_메시지_조립_실패() throws Exception {
        // given
        given(moderateReportUseCase.moderate(any())).willReturn(new ModerateReportResult(
            901L, ReportStatus.APPROVED, null, ReportActionReason.ABUSE, null, ReportTargetRemoval.NONE, "모루",
            Instant.now()
        ));

        // when & then
        assertEphemeral(respond(reasonSubmit(APPROVE_BUTTON, MODERATOR_ROLE_ID, "ABUSE", null)),
            DiscordInteractionHandler.MESSAGE_APPLIED_WITHOUT_UPDATE);
    }

    @Test
    @DisplayName("결과 문구는 사유 다음 줄에 삭제 결과를 붙이고, 삭제하지 않았으면 아무것도 붙이지 않는다")
    void 결과_문구에_삭제_결과를_붙인다() {
        // when & then
        assertThat(DiscordInteractionHandler.resultText(approvedResult("욕설 확인", ReportTargetRemoval.DELETED)))
            .isEqualTo("처리 완료 by 모루 · 욕설·비방·혐오 표현\n메시지를 삭제했어요.\n메모: 욕설 확인");
        assertThat(DiscordInteractionHandler.resultText(approvedResult(null, ReportTargetRemoval.ALREADY_DELETED)))
            .endsWith("\n메시지가 이미 삭제되어 있었어요.");
        assertThat(DiscordInteractionHandler.resultText(approvedResult(null, ReportTargetRemoval.NOT_FOUND)))
            .endsWith("\n메시지를 찾을 수 없어 삭제하지 않았어요.");
        assertThat(DiscordInteractionHandler.resultText(approvedResult(null, ReportTargetRemoval.NONE)))
            .doesNotContain("메시지");
    }

    private JsonNode respond(JsonNode interaction) {
        Optional<Map<String, Object>> response = sut.handle(interaction);
        assertThat(response).isPresent();
        return objectMapper.valueToTree(response.get());
    }

    private static void assertEphemeral(JsonNode response, String message) {
        assertThat(response.path("type").asInt()).isEqualTo(DiscordInteractionResponses.CHANNEL_MESSAGE);
        assertThat(response.at("/data/flags").asInt()).isEqualTo(DiscordInteractionResponses.EPHEMERAL_FLAG);
        assertThat(response.at("/data/content").asText()).isEqualTo(message);
    }

    private JsonNode buttonClick(String customId, String guildId, String roleId) throws Exception {
        return json("""
            {
              "type": 3,
              "guild_id": "%s",
              "member": {"roles": ["%s"], "nick": "모루", "user": {"id": "300", "username": "moru"}},
              "data": {"custom_id": "%s", "component_type": 2}
            }
            """.formatted(guildId, roleId, customId));
    }

    private static ModerateReportResult approvedResult(String memo) {
        return approvedResult(memo, ReportTargetRemoval.NONE);
    }

    private static ModerateReportResult approvedResult(String memo, ReportTargetRemoval targetRemoval) {
        return new ModerateReportResult(
            901L, ReportStatus.APPROVED, ReportActionType.APPROVE, ReportActionReason.ABUSE, memo, targetRemoval,
            "모루", Instant.now()
        );
    }

    /**
     * Discord가 label 컴포넌트로 감싼 입력창을 제출하는 형식입니다. 고르지 않았거나 입력하지 않은 값은 {@code null}로 넘깁니다.
     */
    private JsonNode reasonSubmit(String customId, String roleId, String reason, String memo) throws Exception {
        String reasonValues = objectMapper.writeValueAsString(reason == null ? List.of() : List.of(reason));
        String memoJson = objectMapper.writeValueAsString(memo == null ? "" : memo);
        return json("""
            {
              "type": 5,
              "guild_id": "%s",
              "member": {"roles": ["%s"], "nick": "모루", "user": {"id": "300", "username": "moru"}},
              "message": {
                "embeds": [{
                  "type": "rich",
                  "title": "신고 접수 #901",
                  "fields": [{"name": "신고 ID", "value": "901", "inline": true}]
                }]
              },
              "data": {
                "custom_id": "%s",
                "components": [
                  {"type": 18, "component": {"type": 3, "custom_id": "reason", "values": %s}},
                  {"type": 18, "component": {"type": 4, "custom_id": "memo", "value": %s}}
                ]
              }
            }
            """.formatted(GUILD_ID, roleId, customId, reasonValues, memoJson));
    }

    private JsonNode json(String value) throws Exception {
        return objectMapper.readTree(value);
    }
}
