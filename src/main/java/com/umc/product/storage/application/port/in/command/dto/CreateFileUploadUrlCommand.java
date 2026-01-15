package com.umc.product.storage.application.port.in.command.dto;

public record CreateFileUploadUrlCommand(
        String fileName,
        String contentType,
        Long fileSize) {
}
