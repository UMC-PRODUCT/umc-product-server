package com.umc.product.report.adapter.in.web.discord;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.fasterxml.jackson.databind.ObjectMapper;

@DisplayName("Discord 상호작용 콜백")
class DiscordInteractionControllerTest {

    private static final String URL = "/api/v1/reports/discord/interactions";
    private static final Instant NOW = Instant.parse("2026-10-29T00:00:00Z");
    private static final String TIMESTAMP = String.valueOf(NOW.getEpochSecond());

    private final DiscordInteractionTestKeys keys = DiscordInteractionTestKeys.generate();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DiscordInteractionSignatureVerifier verifier = new DiscordInteractionSignatureVerifier(
            new ReportDiscordInteractionProperties(keys.publicKeyHex()),
            Clock.fixed(NOW, ZoneOffset.UTC)
        );
        mockMvc = MockMvcBuilders
            .standaloneSetup(new DiscordInteractionController(verifier, new ObjectMapper()))
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
            .andExpect(content().json("{\"type\":1}", true));
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
    @DisplayName("아직 처리하지 않는 상호작용 유형이면 400으로 응답한다")
    void 처리하지_않는_유형이면_400() throws Exception {
        // given
        String body = "{\"id\":\"1\",\"type\":3}";

        // when & then
        mockMvc.perform(signedRequest(body, keys.sign(TIMESTAMP, body)))
            .andExpect(status().isBadRequest());
    }

    private RequestBuilder signedRequest(String body, String signature) {
        return post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(DiscordInteractionController.SIGNATURE_HEADER, signature)
            .header(DiscordInteractionController.TIMESTAMP_HEADER, TIMESTAMP)
            .content(body);
    }
}
