package com.umc.product.storage.application.port.in.query.dto;

import com.umc.product.storage.domain.enums.FileCategory;
import java.time.Instant;

/**
 * 파일 정보 DTO
 *
 * @param fileId           파일 ID
 * @param originalFileName 원본 파일 이름
 * @param category         파일 카테고리
 * @param contentType      파일 MIME 타입
 * @param fileSize         파일 크기 (바이트 단위)
 * @param fileLink         파일 접근 링크
 * @param isUploaded       파일 업로드 여부
 * @param uploadedMemberId 업로드한 회원 ID
 * @param createdAt        파일 생성 시각
 */
public record FileInfo(
        String fileId,
        String originalFileName,
        FileCategory category,
        String contentType,
        Long fileSize,
        String fileLink,
        Boolean isUploaded,
        Long uploadedMemberId,
        Instant createdAt
) {
    // TODO: fileLink의 경우 S3/GCS 등에 따라서 동적으로 제작해서 제공할 필요가 있음
}
