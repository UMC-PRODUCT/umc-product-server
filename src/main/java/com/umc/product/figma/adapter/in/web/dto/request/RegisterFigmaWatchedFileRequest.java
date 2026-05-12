package com.umc.product.figma.adapter.in.web.dto.request;

import com.umc.product.figma.application.port.in.dto.RegisterFigmaWatchedFileCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterFigmaWatchedFileRequest(
    @NotBlank @Size(max = 100) String fileKey,
    @NotBlank @Size(max = 255) String displayName
) {
    public RegisterFigmaWatchedFileCommand toCommand() {
        return new RegisterFigmaWatchedFileCommand(fileKey, displayName);
    }
}
