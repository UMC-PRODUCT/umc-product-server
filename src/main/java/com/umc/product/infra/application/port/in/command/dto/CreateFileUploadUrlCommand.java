package com.umc.product.infra.application.port.in.command.dto;

public record CreateFileUploadUrlCommand(
        String fileName,
        String contentType,
        Long fileSize) {
}
