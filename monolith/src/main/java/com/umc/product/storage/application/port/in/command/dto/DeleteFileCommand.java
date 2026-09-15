package com.umc.product.storage.application.port.in.command.dto;

import java.util.Objects;

import lombok.Builder;

/**
 * 등록된 파일 삭제 Command.
 */
@Builder
public record DeleteFileCommand(
    String fileId,
    Long requesterMemberId
) {
    public DeleteFileCommand {
        Objects.requireNonNull(fileId, "fileId must not be null");
        Objects.requireNonNull(requesterMemberId, "requesterMemberId must not be null");

        if (fileId.isBlank()) {
            throw new IllegalArgumentException("fileId must not be blank");
        }
    }
}
