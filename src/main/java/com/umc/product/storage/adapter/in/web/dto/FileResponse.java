package com.umc.product.storage.adapter.in.web.dto;

import com.umc.product.storage.application.port.in.query.dto.FileInfo;
import com.umc.product.storage.domain.enums.FileCategory;

public record FileResponse(
        String fileId,
        String originalFileName,
        FileCategory category,
        String contentType,
        Long fileSize,
        String fileUrl
) {
    public static FileResponse from(FileInfo info) {
        return new FileResponse(
                info.fileId(),
                info.originalFileName(),
                info.category(),
                info.contentType(),
                info.fileSize(),
                info.fileLink()
        );
    }
}
