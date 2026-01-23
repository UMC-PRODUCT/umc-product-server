package com.umc.product.storage.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.storage.domain.enums.FileCategory;
import com.umc.product.storage.domain.enums.StorageProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 파일 메타데이터 엔티티
 *
 * <p>실제 파일은 외부 스토리지(S3, GCS 등)에 저장되며,
 * 이 엔티티는 파일의 메타 정보와 접근 경로를 관리합니다.
 */
@Entity
@Table(name = "file_metadata")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileMetadata extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 파일 고유 ID (외부 스토리지 key 생성에 사용)
     */
    @Column(nullable = false, unique = true, length = 36)
    private String fileId;

    /**
     * 원본 파일명
     */
    @Column(nullable = false)
    private String originalFileName;

    /**
     * 파일 카테고리 (프로필 이미지, 게시글 이미지 등)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FileCategory category;

    /**
     * Content-Type (MIME type)
     */
    @Column(nullable = false, length = 100)
    private String contentType;

    /**
     * 파일 크기 (bytes)
     */
    @Column(nullable = false)
    private Long fileSize;


    /**
     * 스토리지 제공자 (S3, GCS 등)
     */
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private StorageProvider storageProvider;

    /**
     * 스토리지 키 (S3 key, GCS object name 등)
     */
    @Column(nullable = false, unique = true, length = 500)
    private String storageKey;

    /**
     * 업로드한 사용자 ID (선택)
     */
    @Column
    private Long uploadedMemberId;

    /**
     * 업로드 완료 여부
     */
    @Column(nullable = false)
    private boolean isUploaded = false;

    @Builder
    private FileMetadata(
            String fileId,
            String originalFileName,
            FileCategory category,
            String contentType,
            Long fileSize,
            StorageProvider storageProvider,
            String storageKey,
            Long uploadedMemberId
    ) {
        this.fileId = fileId;
        this.originalFileName = originalFileName;
        this.category = category;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.storageProvider = storageProvider;
        this.storageKey = storageKey;
        this.uploadedMemberId = uploadedMemberId;
        this.isUploaded = false;
    }

    /**
     * 업로드 완료 처리
     */
    public void markAsUploaded() {
        this.isUploaded = true;
    }

    /**
     * 파일 확장자 추출
     */
    public String getFileExtension() {
        int lastDotIndex = originalFileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < originalFileName.length() - 1) {
            return originalFileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
}
