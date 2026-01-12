package com.umc.product.infra.application.port.in.query.dto;

import com.umc.product.infra.domain.FileMetadata;
import com.umc.product.infra.domain.enums.FileCategory;
import java.time.Instant;

/**
 * 파일 정보 DTO
 */
public record FileInfo(
        String fileId,
        String originalFileName,
        FileCategory category,
        String contentType,
        Long fileSize,
        boolean uploaded,
        Long uploadedBy,
        Instant createdAt
) {
    public static FileInfo from(FileMetadata fileMetadata) {
        return new FileInfo(
                fileMetadata.getFileId(),
                fileMetadata.getOriginalFileName(),
                fileMetadata.getCategory(),
                fileMetadata.getContentType(),
                fileMetadata.getFileSize(),
                fileMetadata.isUploaded(),
                fileMetadata.getUploadedBy(),
                fileMetadata.getCreatedAt()
        );
    }
}
