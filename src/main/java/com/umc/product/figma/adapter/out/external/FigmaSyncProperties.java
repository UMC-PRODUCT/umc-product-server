package com.umc.product.figma.adapter.out.external;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
