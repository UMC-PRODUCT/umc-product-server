package com.umc.product.github.adapter.in.web;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.github.application.port.in.HandleGithubWebhookUseCase;
import com.umc.product.github.application.port.in.dto.GithubWebhookCommand;
import com.umc.product.global.security.annotation.Public;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * GitHub App 웹훅 수신 엔드포인트 (ADR-010 Phase A — 활동 메트릭 수집).
 *
 * <p>처리 흐름:
 * <ol>
 *     <li>{@code X-Hub-Signature-256} 서명을 raw body 기준으로 검증한다 (실패 시 401).</li>
 *     <li>검증을 통과하면 즉시 200 을 반환하고, 본격 처리는 {@link HandleGithubWebhookUseCase} 의 비동기 처리에 위임한다
 *         (GitHub 의 webhook 응답 SLA 준수).</li>
 * </ol>
 *
 * <p>JWT 인증 대상이 아니므로 {@link Public} 으로 permitAll 처리한다. 신뢰 근거는 HMAC 서명이다.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "GitHub | Webhook", description = "GitHub App 웹훅 수신 (PR/Issue 활동 메트릭)")
public class GithubWebhookController {

    private static final String HEADER_SIGNATURE = "X-Hub-Signature-256";
    private static final String HEADER_EVENT = "X-GitHub-Event";
    private static final String HEADER_DELIVERY = "X-GitHub-Delivery";

    private final GithubWebhookSignatureVerifier signatureVerifier;
    private final HandleGithubWebhookUseCase handleGithubWebhookUseCase;

    @PostMapping("/webhooks/github")
    @Public
    @Operation(
        summary = "GitHub 웹훅 수신",
        description = "X-Hub-Signature-256 서명 검증 후 PR/Issue 이벤트를 메트릭으로 집계한다. 서명 실패 시 401."
    )
    public ResponseEntity<Void> receive(
        @RequestBody byte[] payload,
        @RequestHeader(value = HEADER_SIGNATURE, required = false) String signature,
        @RequestHeader(value = HEADER_EVENT, required = false) String eventType,
        @RequestHeader(value = HEADER_DELIVERY, required = false) String deliveryId
    ) {
        if (!signatureVerifier.isValid(payload, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        handleGithubWebhookUseCase.handle(
            new GithubWebhookCommand(eventType, deliveryId, new String(payload, StandardCharsets.UTF_8))
        );
        return ResponseEntity.ok().build();
    }
}
