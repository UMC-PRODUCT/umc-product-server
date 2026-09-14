package com.umc.product.storage.adapter.in.web.dto;

import com.umc.product.storage.application.port.in.command.dto.FileUploadInfo;
import java.time.LocalDateTime;
import java.util.Map;

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
