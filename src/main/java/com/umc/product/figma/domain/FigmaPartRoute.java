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
 * 페이지/프레임 이름 → 담당 파트 + Discord role 매핑.
 * fallback=true 인 행은 매핑되지 않은 댓글이 도달할 기본 라우트.
 * fallback 행의 page_name은 관용적으로 "*" 등을 사용한다.
 */
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "figma_part_route")
public class FigmaPartRoute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_key", nullable = false, length = 100)
    private String fileKey;

    @Column(name = "page_name", nullable = false)
    private String pageName;

    @Column(name = "part_key", nullable = false, length = 50)
    private String partKey;

    @Column(name = "discord_role_id", nullable = false, length = 50)
    private String discordRoleId;

    @Column(name = "discord_webhook_url", nullable = false, columnDefinition = "TEXT")
    private String discordWebhookUrl;

    @Column(name = "fallback", nullable = false)
    private boolean fallback;

    public static FigmaPartRoute of(
        String fileKey,
        String pageName,
        String partKey,
        String discordRoleId,
        String discordWebhookUrl,
        boolean fallback
    ) {
        return FigmaPartRoute.builder()
            .fileKey(fileKey)
            .pageName(pageName)
            .partKey(partKey)
            .discordRoleId(discordRoleId)
            .discordWebhookUrl(discordWebhookUrl)
            .fallback(fallback)
            .build();
    }
}
