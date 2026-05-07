package com.umc.product.figma.application.port.in.dto;

import com.umc.product.figma.domain.FigmaWatchedFile;
import java.time.Instant;

public record FigmaWatchedFileInfo(
    Long id,
    String fileKey,
    String displayName,
    boolean enabled,
    Instant lastSyncedAt,
    String lastError
) {

    public static FigmaWatchedFileInfo from(FigmaWatchedFile watchedFile) {
        return new FigmaWatchedFileInfo(
            watchedFile.getId(),
            watchedFile.getFileKey(),
            watchedFile.getDisplayName(),
            watchedFile.isEnabled(),
            watchedFile.getLastSyncedAt(),
            watchedFile.getLastError()
        );
    }
}
