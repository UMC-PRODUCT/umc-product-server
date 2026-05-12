package com.umc.product.figma.application.port.in.dto;

public record RegisterFigmaWatchedFileCommand(
    String fileKey,
    String displayName
) {
}
