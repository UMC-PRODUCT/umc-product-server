package com.umc.product.report.adapter.in.web.discord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.umc.product.report.adapter.discord.ReportDiscordButtons;
import com.umc.product.report.application.port.in.command.ModerateReportUseCase;
import com.umc.product.report.application.port.in.command.dto.ModerateReportResult;
import com.umc.product.report.domain.enums.ReportActionReason;
import com.umc.product.report.domain.enums.ReportActionType;
import com.umc.product.report.domain.enums.ReportStatus;
import com.umc.product.report.domain.enums.ReportTargetRemoval;

@DisplayName("Discord 상호작용 콜백")
class DiscordInteractionControllerTest {

    private static final String URL = "/api/v1/reports/discord/interactions";
    private static final Instant NOW = Instant.parse("2026-10-29T00:00:00Z");
    // "type":"1" 처럼 숫자 필드가 문자열로 나간 경우
    private static final String QUOTED_NUMBER = "\"(type|flags|style|color|min_values|max_values|max_length)\":\"\\d+\"";
    private static final String TIMESTAMP = String.valueOf(NOW.getEpochSecond());

    private final DiscordInteractionTestKeys keys = DiscordInteractionTestKeys.generate();
    private MockMvc mockMvc;
    private ModerateReportUseCase moderateReportUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = buildMockMvc(new ObjectMapper());
    }

    private MockMvc buildMockMvc(ObjectMapper objectMapper) {
        moderateReportUseCase = mock(ModerateReportUseCase.class);
        ReportDiscordInteractionProperties properties =
            new ReportDiscordInteractionProperties(keys.publicKeyHex(), "guild-1", "role-1");
        DiscordInteractionSignatureVerifier verifier =
            new DiscordInteractionSignatureVerifier(properties, Clock.fixed(NOW, ZoneOffset.UTC));
        DiscordInteractionHandler handler =
            new DiscordInteractionHandler(moderateReportUseCase, properties, objectMapper);
        return MockMvcBuilders
            .standaloneSetup(new DiscordInteractionController(verifier, handler, objectMapper))
            .build();
    }

    @Test
    @DisplayName("서명이 맞는 PING이면 감싸지 않은 {\"type\":1}로 응답한다")
    void PING이면_PONG으로_응답한다() throws Exception {
        // given
        String body = "{\"id\":\"1\",\"type\":1}";

        // when & then
        mockMvc.perform(signedRequest(body, keys.sign(TIMESTAMP, body)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{\"type\":1}", true));
        assertThat(responseBody(body)).isEqualTo("{\"type\":1}");
    }

    @Test
    @DisplayName("WRITE_NUMBERS_AS_STRINGS가 켜진 ObjectMapper가 주입돼도 숫자를 숫자로 응답한다")
    void 전역_설정이_숫자를_문자열로_써도_숫자로_응답한다() throws Exception {
        // given
        ObjectMapper numbersAsStrings = JsonMapper.builder()
            .enable(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS.mappedFeature())
            .build();
        mockMvc = buildMockMvc(numbersAsStrings);
        String body = "{\"id\":\"1\",\"type\":1}";

        // when & then
        assertThat(responseBody(body)).isEqualTo("{\"type\":1}");
    }

    @Test
    @DisplayName("버튼 클릭 응답의 모달 type 9와 컴포넌트 type이 숫자로 나간다")
    void 버튼_클릭이면_모달을_숫자로_응답한다() throws Exception {
        // given
        mockMvc = buildMockMvc(numbersAsStringsMapper());
        String body = """
            {"type":3,"guild_id":"guild-1",
             "member":{"roles":["role-1"],"user":{"id":"300","username":"moru"}},
             "data":{"custom_id":"%s","component_type":2}}
            """.formatted(ReportDiscordButtons.customId(901L, ReportActionType.APPROVE));

        // when
        String response = responseBody(body);

        // then
        assertNumeric(response, "/type", 9);
        assertNumeric(response, "/data/components/0/type", 18);
        assertNumeric(response, "/data/components/0/component/type", 3);
        assertThat(response).doesNotContainPattern(QUOTED_NUMBER);
    }

    @Test
    @DisplayName("사유 제출 응답의 메시지 갱신 type 7과 flags 64가 숫자로 나간다")
    void 사유_제출이면_메시지_갱신과_flags를_숫자로_응답한다() throws Exception {
        // given
        mockMvc = buildMockMvc(numbersAsStringsMapper());
        given(moderateReportUseCase.moderate(any())).willReturn(new ModerateReportResult(
            901L, ReportStatus.APPROVED, ReportActionType.APPROVE, ReportActionReason.ABUSE, null,
            ReportTargetRemoval.NONE, "모루", NOW
        ));
        String customId = ReportDiscordButtons.customId(901L, ReportActionType.APPROVE);
        String submit = """
            {"type":5,"guild_id":"guild-1",
             "member":{"roles":["role-1"],"nick":"모루","user":{"id":"300","username":"moru"}},
             "data":{"custom_id":"%s","components":[{"type":1,"components":[{"custom_id":"reason","values":["ABUSE"]}]}]}}
            """.formatted(customId);
        String noPermission = submit.replace("role-1", "role-other");

        // when
        String updated = responseBody(submit);
        String ephemeral = responseBody(noPermission);

        // then
        assertNumeric(updated, "/type", 7);
        assertThat(updated).doesNotContainPattern(QUOTED_NUMBER);
        assertNumeric(ephemeral, "/type", 4);
        assertNumeric(ephemeral, "/data/flags", 64);
        assertThat(ephemeral).doesNotContainPattern(QUOTED_NUMBER);
    }

    @Test
    @DisplayName("서명이 틀리면 401로 거절한다")
    void 서명이_틀리면_401() throws Exception {
        // given
        String body = "{\"id\":\"1\",\"type\":1}";
        String signatureForOtherBody = keys.sign(TIMESTAMP, "{\"id\":\"2\",\"type\":1}");

        // when & then
        mockMvc.perform(signedRequest(body, signatureForOtherBody))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("서명 헤더가 없으면 401로 거절한다")
    void 서명_헤더가_없으면_401() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"type\":1}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("처리하지 않는 상호작용 유형(슬래시 명령 등)이면 400으로 응답한다")
    void 처리하지_않는_유형이면_400() throws Exception {
        // given
        String body = "{\"id\":\"1\",\"type\":2}";

        // when & then
        mockMvc.perform(signedRequest(body, keys.sign(TIMESTAMP, body)))
            .andExpect(status().isBadRequest());
    }

    // 응답 Map의 키 순서는 정해져 있지 않으므로 순서에 의존하지 않고 파싱해서 확인합니다.
    private static void assertNumeric(String json, String pointer, int expected) throws Exception {
        JsonNode node = new ObjectMapper().readTree(json).at(pointer);
        assertThat(node.isInt()).as("%s 는 숫자여야 한다: %s", pointer, node).isTrue();
        assertThat(node.intValue()).isEqualTo(expected);
    }

    private static ObjectMapper numbersAsStringsMapper() {
        return JsonMapper.builder()
            .enable(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS.mappedFeature())
            .build();
    }

    private String responseBody(String body) throws Exception {
        byte[] bytes = mockMvc.perform(signedRequest(body, keys.sign(TIMESTAMP, body)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private RequestBuilder signedRequest(String body, String signature) {
        return post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(DiscordInteractionController.SIGNATURE_HEADER, signature)
            .header(DiscordInteractionController.TIMESTAMP_HEADER, TIMESTAMP)
            .content(body);
    }
}
