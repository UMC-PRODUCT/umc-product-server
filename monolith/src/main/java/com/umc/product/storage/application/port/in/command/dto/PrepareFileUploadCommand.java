package com.umc.product.storage.application.port.in.command.dto;

import com.umc.product.storage.domain.enums.FileCategory;
import java.util.Objects;

/**
 * 파일 업로드 준비 커맨드
 */
public record PrepareFileUploadCommand(
        String fileName,
        String contentType,
        Long fileSize,
        FileCategory category,
        Long uploadedBy
) {
    public PrepareFileUploadCommand {
        Objects.requireNonNull(fileName, "fileName must not be null");
        Objects.requireNonNull(contentType, "contentType must not be null");
        Objects.requireNonNull(fileSize, "fileSize must not be null");
        Objects.requireNonNull(category, "category must not be null");

        if (fileName.isBlank()) {
            throw new IllegalArgumentException("fileName must not be blank");
        }
        if (fileSize <= 0) {
            throw new IllegalArgumentException("fileSize must be greater than 0");
        }
    }

    /**
     * uploadedBy 없이 생성하는 생성자
     */
    public PrepareFileUploadCommand(
            String fileName,
            String contentType,
            Long fileSize,
            FileCategory category) {
        this(fileName, contentType, fileSize, category, null);
    }
}
