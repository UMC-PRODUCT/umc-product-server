package com.umc.product.figma.adapter.out.external;

import com.umc.product.figma.application.port.out.SendDiscordMentionPort;
import com.umc.product.figma.application.port.out.dto.DiscordMentionMessage;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Figma 댓글을 Discord 로 포워딩할 때 사용하는 멘션 가능한 webhook 어댑터.
 * 본 commit 에서는 평문 content 만 발송하고, commit 6 에서 embed 포맷으로 확장된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordMentionWebhookAdapter implements SendDiscordMentionPort {

    private final RestClient restClient;

    @Override
    public void send(DiscordMentionMessage message) {
        String mentionLine = String.join(" ", message.mentionRenders());
        String body = String.format(
            "%s%n[Figma] %s / %s%n👤 %s%n💬 %s%n🔗 %s",
            mentionLine,
            message.fileDisplayName(),
            message.pageName() == null ? "(unmapped)" : message.pageName(),
            message.authorName(),
            message.message(),
            message.commentLink()
        );

        Map<String, Object> payload = Map.of(
            "content", body,
            "allowed_mentions", Map.of("parse", List.of("roles", "users"))
        );

        try {
            restClient.post()
                .uri(message.webhookUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
            log.debug("Discord 멘션 전송 완료: domainKey={}, mentions={}",
                message.domainKey(), message.mentionRenders().size());
        } catch (RestClientResponseException e) {
            log.error("Discord 멘션 전송 실패: status={}, body={}",
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new FigmaDomainException(FigmaErrorCode.DISCORD_MENTION_SEND_FAILED);
        }
    }
}
