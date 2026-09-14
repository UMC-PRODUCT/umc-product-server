package com.umc.product.storage.adapter.in.web.dto;

import com.umc.product.storage.application.port.in.command.dto.PrepareFileUploadCommand;
import com.umc.product.storage.domain.enums.FileCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PrepareUploadRequest(
        @NotBlank(message = "파일명은 필수입니다.")
        String fileName,

        @NotBlank(message = "Content-Type은 필수입니다.")
        String contentType,

        @NotNull(message = "파일 크기는 필수입니다.")
        @Positive(message = "파일 크기는 0보다 커야 합니다.")
        Long fileSize,

        @NotNull(message = "파일 카테고리는 필수입니다.")
        FileCategory category
) {
    public PrepareFileUploadCommand toCommand(Long memberId) {
        return new PrepareFileUploadCommand(
                fileName,
                contentType,
                fileSize,
                category,
                memberId
        );
    }
}
