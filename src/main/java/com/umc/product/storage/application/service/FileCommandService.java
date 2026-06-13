package com.umc.product.storage.application.service;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.storage.application.port.in.command.ManageFileUseCase;
import com.umc.product.storage.application.port.in.command.dto.DeleteFileCommand;
import com.umc.product.storage.application.port.in.command.dto.FileUploadInfo;
import com.umc.product.storage.application.port.in.command.dto.PrepareFileUploadCommand;
import com.umc.product.storage.application.port.out.LoadFileMetadataPort;
import com.umc.product.storage.application.port.out.SaveFileMetadataPort;
import com.umc.product.storage.application.port.out.StoragePort;
import com.umc.product.storage.application.port.out.dto.StorageObjectInfo;
import com.umc.product.storage.domain.FileMetadata;
import com.umc.product.storage.domain.enums.FileCategory;
import com.umc.product.storage.domain.enums.StorageProvider;
import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileCommandService implements ManageFileUseCase {

    private static final long UPLOAD_URL_DURATION_MINUTES = 15;

    private final StoragePort storagePort;
    private final LoadFileMetadataPort loadFileMetadataPort;
    private final SaveFileMetadataPort saveFileMetadataPort;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;

    @Override
    @Transactional
    public FileUploadInfo getFileUploadUrl(PrepareFileUploadCommand command) {
        // 파일 검증
        validateFile(command);

        // 파일 ID 생성
        String fileId = UUID.randomUUID().toString();

        // 확장자 추출
        String extension = extractExtension(command.fileName());

        // 스토리지 키 생성
        String storageKey = storagePort.generateStorageKey(
            command.category(),
            fileId,
            extension
        );

        // 메타데이터 저장
        FileMetadata metadata = FileMetadata.builder()
            .fileId(fileId)
            .originalFileName(command.fileName())
            .category(command.category())
            .contentType(command.contentType())
            .fileSize(command.fileSize())
            .storageProvider(StorageProvider.AWS_S3)
            .storageKey(storageKey)
            .uploadedMemberId(command.uploadedBy())
            .build();

        saveFileMetadataPort.save(metadata);

        // Signed URL 생성
        FileUploadInfo uploadInfo = storagePort.generateUploadUrl(
            storageKey,
            command.contentType(),
            command.fileSize(),
            UPLOAD_URL_DURATION_MINUTES
        );

        log.info("파일 업로드 URL 생성 완료: fileId={}, category={}", fileId, command.category());

        return new FileUploadInfo(
            fileId,
            uploadInfo.uploadUrl(),
            uploadInfo.uploadMethod(),
            uploadInfo.headers(),
            uploadInfo.expiresAt()
        );
    }

    @Override
    @Transactional
    public void confirmUpload(String fileId) {
        FileMetadata metadata = loadFileMetadataPort.findByFileId(fileId)
            .orElseThrow(() -> new StorageException(StorageErrorCode.FILE_NOT_FOUND));

        if (metadata.isUploaded()) {
            throw new StorageException(StorageErrorCode.FILE_ALREADY_UPLOADED);
        }

        StorageObjectInfo objectInfo = storagePort.findObjectInfoByStorageKey(metadata.getStorageKey())
            .orElseThrow(() -> new StorageException(StorageErrorCode.FILE_UPLOAD_NOT_COMPLETED));

        confirmUploaded(metadata, objectInfo);
        saveFileMetadataPort.save(metadata);

        log.info("파일 업로드 완료 확인: fileId={}", fileId);
    }

    @Override
    public void deleteFile(DeleteFileCommand command) {
        FileMetadata metadata = loadFileMetadataPort.findByFileId(command.fileId())
            .orElseThrow(() -> new StorageException(StorageErrorCode.FILE_NOT_FOUND));

        validateDeletePermission(metadata, command.requesterMemberId());

        // 스토리지에서 파일 삭제
        storagePort.delete(metadata.getStorageKey());

        // 메타데이터 삭제
        saveFileMetadataPort.deleteByFileId(command.fileId());

        log.info("파일 삭제 완료: fileId={}", command.fileId());
    }

    private void validateDeletePermission(FileMetadata metadata, Long requesterMemberId) {
        if (Objects.equals(metadata.getUploadedMemberId(), requesterMemberId) || isSuperAdmin(requesterMemberId)) {
            return;
        }

        throw new StorageException(StorageErrorCode.FILE_DELETE_FORBIDDEN);
    }

    private void confirmUploaded(FileMetadata metadata, StorageObjectInfo objectInfo) {
        try {
            metadata.confirmUploaded(objectInfo.contentLength(), objectInfo.contentType());
        } catch (StorageException e) {
            deleteInvalidUpload(metadata.getStorageKey(), e);
            throw e;
        }
    }

    private void deleteInvalidUpload(String storageKey, StorageException validationException) {
        try {
            storagePort.delete(storageKey);
        } catch (Exception deleteException) {
            log.warn(
                "검증 실패 파일 삭제 실패: storageKey={}, validationError={}",
                storageKey,
                validationException.getBaseCode().getCode(),
                deleteException
            );
        }
    }

    private boolean isSuperAdmin(Long memberId) {
        return getChallengerRoleUseCase.findAllByMemberId(memberId).stream()
            .map(ChallengerRoleInfo::roleType)
            .filter(Objects::nonNull)
            .anyMatch(roleType -> roleType.isSuperAdmin());
    }

    private void validateFile(PrepareFileUploadCommand command) {
        String extension = extractExtension(command.fileName());

        // 확장자가 명시적으로 필요한 카테고리는 빈 확장자 거부
        if (extension.isBlank() && requiresExtension(command.category())) {
            throw new StorageException(StorageErrorCode.INVALID_FILE_EXTENSION);
        }

        if (!command.category().isAllowedExtension(extension)) {
            throw new StorageException(StorageErrorCode.INVALID_FILE_EXTENSION);
        }

        if (!command.category().isAllowedSize(command.fileSize())) {
            throw new StorageException(StorageErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    /**
     * 확장자가 필수인 카테고리인지 확인
     */
    private boolean requiresExtension(FileCategory category) {
        // ETC만 확장자 선택 사항, 나머지는 필수
        return category != FileCategory.ETC;
    }

    private String extractExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');

        // 파일명이 .으로 시작하거나(숨김 파일), .으로 끝나는 경우 확장자 없음
        if (lastDotIndex <= 0 || lastDotIndex >= fileName.length() - 1) {
            return "";
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}
