package com.umc.product.report.adapter.in.web.discord;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.security.annotation.Public;

import io.swagger.v3.oas.annotations.Hidden;

/**
 * Discord가 호출하는 상호작용 콜백(Interactions Endpoint URL)입니다.
 * <p>
 * 서명은 원문 body로 검증해야 하므로 body를 byte[]로 받고, 검증을 통과한 뒤에만 JSON으로 읽습니다. Discord가 정한 응답 형식을 그대로 돌려줘야
 * 해서 {@code ApiResponse}로 감싸지 않도록 {@link ResponseEntity}를 반환합니다.
 */
@Hidden
@RestController
@RequestMapping("/api/v1/reports/discord")
public class DiscordInteractionController {

    static final String SIGNATURE_HEADER = "X-Signature-Ed25519";
    static final String TIMESTAMP_HEADER = "X-Signature-Timestamp";

    private static final int INTERACTION_TYPE_PING = 1;
    private static final int RESPONSE_TYPE_PONG = 1;

    private final DiscordInteractionSignatureVerifier signatureVerifier;
    private final ObjectMapper objectMapper;

    public DiscordInteractionController(
        DiscordInteractionSignatureVerifier signatureVerifier,
        ObjectMapper objectMapper
    ) {
        this.signatureVerifier = signatureVerifier;
        this.objectMapper = objectMapper;
    }

    @Public
    @PostMapping(value = "/interactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> handle(
        @RequestHeader(value = SIGNATURE_HEADER, required = false) String signature,
        @RequestHeader(value = TIMESTAMP_HEADER, required = false) String timestamp,
        @RequestBody(required = false) byte[] body
    ) {
        if (!signatureVerifier.isValid(signature, timestamp, body)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        JsonNode interaction = readJson(body);
        if (interaction == null) {
            return ResponseEntity.badRequest().build();
        }
        if (interaction.path("type").asInt() == INTERACTION_TYPE_PING) {
            return ResponseEntity.ok(Map.of("type", RESPONSE_TYPE_PONG));
        }
        // 버튼·모달 상호작용은 아직 처리하지 않습니다.
        return ResponseEntity.badRequest().build();
    }

    private JsonNode readJson(byte[] body) {
        try {
            return objectMapper.readTree(body);
        } catch (IOException exception) {
            return null;
        }
    }
}
