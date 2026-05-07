package com.umc.product.figma.adapter.in.web.dto.response;

import com.umc.product.figma.application.port.in.dto.FigmaWatchedFileInfo;
import java.time.Instant;

public record FigmaWatchedFileResponse(
    Long id,
    String fileKey,
    String displayName,
    boolean enabled,
    String lastSyncedCommentId,
    Instant lastSyncedAt,
    String lastError
) {

    public static FigmaWatchedFileResponse from(FigmaWatchedFileInfo info) {
        return new FigmaWatchedFileResponse(
            info.id(),
            info.fileKey(),
            info.displayName(),
            info.enabled(),
            info.lastSyncedCommentId(),
            info.lastSyncedAt(),
            info.lastError()
        );
    }
}
