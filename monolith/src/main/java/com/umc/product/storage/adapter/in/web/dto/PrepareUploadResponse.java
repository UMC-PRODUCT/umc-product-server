package com.umc.product.storage.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.umc.product.storage.application.port.in.command.dto.FileUploadInfo;

public record PrepareUploadResponse(
        String fileId,
        String uploadUrl,
        String uploadMethod,
        Map<String, String> headers,
        LocalDateTime expiresAt
) {
    public static PrepareUploadResponse from(FileUploadInfo info) {
        return new PrepareUploadResponse(
                info.fileId(),
                info.uploadUrl(),
                info.uploadMethod(),
                info.headers(),
                info.expiresAt()
        );
    }
}
