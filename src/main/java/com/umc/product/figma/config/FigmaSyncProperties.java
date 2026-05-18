package com.umc.product.figma.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Figma 댓글 동기화 스케줄러 설정.
 * adapter/in, adapter/out, application/service 중 어느 레이어에서도 참조할 수 있도록
 * 중립 config 패키지에 위치한다.
 */
@ConfigurationProperties(prefix = "app.figma.sync")
public record FigmaSyncProperties(
    boolean enabled,
    Duration pollInterval,
    int maxFilesPerRun
) {
    public FigmaSyncProperties {
        if (pollInterval == null) {
            pollInterval = Duration.ofMinutes(5);
        }
        if (maxFilesPerRun <= 0) {
            maxFilesPerRun = 50;
        }
    }
}
