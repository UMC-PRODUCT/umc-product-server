package com.umc.product.figma.adapter.in.web.dto.response;

import com.umc.product.figma.adapter.in.web.support.DiscordWebhookUrlMasker;
import com.umc.product.figma.application.port.in.dto.FigmaRoutingDomainSummaryInfo;
import java.util.List;

/**
 * 운영진 화면에 노출되는 라우팅 도메인 응답. {@code discordWebhookUrlMasked} 는 ADR-005 의 마스킹 정책에 따라 변형된 값이며, 원본 webhook URL 은 응답에 절대
 * 포함되지 않는다. 단건 조회에서는 {@code mentions} 가 채워지고, list 응답에서는 {@code null} 이다.
 */
public record FigmaRoutingDomainResponse(
    Long id,
    String domainKey,
    String description,
    String discordWebhookUrlMasked,
    boolean fallback,
    int mentionCount,
    List<FigmaRoutingDomainMentionResponse> mentions
) {

    public static FigmaRoutingDomainResponse from(FigmaRoutingDomainSummaryInfo info) {
        List<FigmaRoutingDomainMentionResponse> mentions = info.mentions() == null
            ? null
            : info.mentions().stream()
                .map(FigmaRoutingDomainMentionResponse::from)
                .toList();

        return new FigmaRoutingDomainResponse(
            info.id(),
            info.domainKey(),
            info.description(),
            DiscordWebhookUrlMasker.mask(info.discordWebhookUrl()),
            info.fallback(),
            info.mentionCount(),
            mentions
        );
    }
}
