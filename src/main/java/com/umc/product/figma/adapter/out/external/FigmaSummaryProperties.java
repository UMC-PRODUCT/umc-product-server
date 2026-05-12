package com.umc.product.figma.adapter.out.external;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 시간창 기반 figma 동기화의 운영 파라미터 (ADR-004 §Implementation Plan §7).
 *
 * @param dispatchRetention     figma_comment_dispatch 행 보존 기간 (기본 90일).
 * @param retentionPollInterval 회수 잡 실행 주기 (기본 24시간).
 */
@ConfigurationProperties(prefix = "app.figma.summary")
public record FigmaSummaryProperties(
    Duration dispatchRetention,
    Duration retentionPollInterval
) {
    public FigmaSummaryProperties {
        if (dispatchRetention == null) {
            dispatchRetention = Duration.ofDays(90);
        }
        if (retentionPollInterval == null) {
            retentionPollInterval = Duration.ofHours(24);
        }
    }
}
