package com.umc.product.figma.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.figma.domain.enums.DiscordMentionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * 라우팅 도메인의 mention 대상 한 건. Cross-domain reference 규칙에 따라 figma_routing_domain 은 ID 로만 참조한다.
 */
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "figma_routing_domain_mention")
public class FigmaRoutingDomainMention extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "domain_id", nullable = false)
    private Long domainId;

    @Column(name = "mention_id", nullable = false, length = 50)
    private String mentionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mention_type", nullable = false, length = 20)
    private DiscordMentionType mentionType;

    @Column(name = "display_label")
    private String displayLabel;

    public static FigmaRoutingDomainMention of(
        Long domainId,
        String mentionId,
        DiscordMentionType mentionType,
        String displayLabel
    ) {
        return FigmaRoutingDomainMention.builder()
            .domainId(domainId)
            .mentionId(mentionId)
            .mentionType(mentionType)
            .displayLabel(displayLabel)
            .build();
    }

    public void update(String mentionId, String displayLabel) {
        this.mentionId = mentionId;
        this.displayLabel = displayLabel;
    }

    public String render() {
        return mentionType.render(mentionId);
    }
}
