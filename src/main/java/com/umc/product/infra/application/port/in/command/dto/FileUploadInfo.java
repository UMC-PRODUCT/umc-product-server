package com.umc.product.infra.application.port.in.command.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 파일 업로드 정보
 *
 * <p>클라이언트가 파일을 업로드하기 위해 필요한 모든 정보를 포함합니다.
 * 구현 방식(presigned URL, signed URL 등)에 독립적인 구조입니다.
 */
public record FileUploadInfo(
        String fileId,
        String uploadUrl,
        String uploadMethod,
        Map<String, String> headers,
        LocalDateTime expiresAt
) {
    /**
     * 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
