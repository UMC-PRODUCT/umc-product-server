package com.umc.product.figma.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * LLM 분류 결과(domain_key) 가 매칭될 라우팅 도메인. 어떤 Discord 채널 (webhook URL) 로 보낼지 와 fallback 여부를 결정한다. 멘션 대상은
 * {@link FigmaRoutingDomainMention} 으로 1:N 으로 분리된다.
 */
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "figma_routing_domain")
public class FigmaRoutingDomain extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "domain_key", nullable = false, length = 100)
    private String domainKey;

    @Column(length = 500)
    private String description;

    @Column(name = "discord_webhook_url", nullable = false, columnDefinition = "TEXT")
    private String discordWebhookUrl;

    @Column(name = "fallback", nullable = false)
    private boolean fallback;

    public static FigmaRoutingDomain of(
        String domainKey,
        String description,
        String discordWebhookUrl,
        boolean fallback
    ) {
        return FigmaRoutingDomain.builder()
            .domainKey(domainKey)
            .description(description)
            .discordWebhookUrl(discordWebhookUrl)
            .fallback(fallback)
            .build();
    }

    public void rename(String description) {
        this.description = description;
    }

    public void changeWebhookUrl(String discordWebhookUrl) {
        this.discordWebhookUrl = discordWebhookUrl;
    }
}
