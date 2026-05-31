package com.umc.product.figma.application.port.in.dto;

import java.util.List;

/**
 * 운영진 화면에서 라우팅 도메인 상태를 조회할 때 사용하는 요약 모델. {@code mentions} 는 단건 조회 시에만 채워지고, list 응답에서는 {@code null} 로 비워둔다 (페이로드 크기
 * 통제).
 */
public record FigmaRoutingDomainSummaryInfo(
    Long id,
    String domainKey,
    String description,
    String discordWebhookUrl,
    boolean fallback,
    int mentionCount,
    List<FigmaRoutingDomainMentionInfo> mentions
) {

    public static FigmaRoutingDomainSummaryInfo listItem(
        Long id,
        String domainKey,
        String description,
        String discordWebhookUrl,
        boolean fallback,
        int mentionCount
    ) {
        return new FigmaRoutingDomainSummaryInfo(
            id,
            domainKey,
            description,
            discordWebhookUrl,
            fallback,
            mentionCount,
            null
        );
    }

    public static FigmaRoutingDomainSummaryInfo detail(
        Long id,
        String domainKey,
        String description,
        String discordWebhookUrl,
        boolean fallback,
        List<FigmaRoutingDomainMentionInfo> mentions
    ) {
        return new FigmaRoutingDomainSummaryInfo(
            id,
            domainKey,
            description,
            discordWebhookUrl,
            fallback,
            mentions.size(),
            mentions
        );
    }
}
