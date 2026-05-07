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
 * Figma 댓글을 Discord로 포워딩할 때 사용하는 멘션 가능한 webhook 어댑터.
 * 기존 {@code DiscordWebhookAdapter}는 단일 webhook URL에 묶여 있어 파트별 채널을 다룰 수 없으므로,
 * 본 어댑터는 입력으로 webhook URL을 받아 파트별 채널로 분기 송신한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordMentionWebhookAdapter implements SendDiscordMentionPort {

    private final RestClient restClient;

    @Override
    public void send(DiscordMentionMessage message) {
        Map<String, Object> body = Map.of(
            "content", message.content(),
            "allowed_mentions", Map.of("parse", List.of("roles"))
        );

        try {
            restClient.post()
                .uri(message.webhookUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
            log.debug("Discord 멘션 전송 완료: roleId={}", message.roleId());
        } catch (RestClientResponseException e) {
            log.error("Discord 멘션 전송 실패: status={}, body={}",
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new FigmaDomainException(FigmaErrorCode.DISCORD_MENTION_SEND_FAILED);
        }
    }
}
