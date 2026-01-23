package com.umc.product.storage.application.service;

import com.umc.product.storage.application.port.in.command.ManageFileUseCase;
import com.umc.product.storage.application.port.in.command.dto.FileUploadInfo;
import com.umc.product.storage.application.port.in.command.dto.PrepareFileUploadCommand;
import com.umc.product.storage.application.port.out.LoadFileMetadataPort;
import com.umc.product.storage.application.port.out.SaveFileMetadataPort;
import com.umc.product.storage.application.port.out.StoragePort;
import com.umc.product.storage.domain.FileMetadata;
import com.umc.product.storage.domain.enums.StorageProvider;
import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FileCommandService implements ManageFileUseCase {

    private static final long UPLOAD_URL_DURATION_MINUTES = 15;

    private final StoragePort storagePort;
    private final LoadFileMetadataPort loadFileMetadataPort;
    private final SaveFileMetadataPort saveFileMetadataPort;

    @Override
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
                .storageProvider(StorageProvider.GOOGLE_CLOUD_STORAGE)
                .storageKey(storageKey)
                .uploadedMemberId(command.uploadedBy())
                .build();

        saveFileMetadataPort.save(metadata);

        // Signed URL 생성
        FileUploadInfo uploadInfo = storagePort.generateUploadUrl(
                storageKey,
                command.contentType(),
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
    public void confirmUpload(String fileId) {
        FileMetadata metadata = loadFileMetadataPort.findByFileId(fileId)
                .orElseThrow(() -> new StorageException(StorageErrorCode.FILE_NOT_FOUND));

        if (metadata.isUploaded()) {
            throw new StorageException(StorageErrorCode.FILE_ALREADY_UPLOADED);
        }

        // 실제 스토리지에 파일이 존재하는지 확인
        if (!storagePort.exists(metadata.getStorageKey())) {
            throw new StorageException(StorageErrorCode.FILE_UPLOAD_NOT_COMPLETED);
        }

        metadata.markAsUploaded();
        saveFileMetadataPort.save(metadata);

        log.info("파일 업로드 완료 확인: fileId={}", fileId);
    }

    @Override
    public void deleteFile(String fileId) {
        FileMetadata metadata = loadFileMetadataPort.findByFileId(fileId)
                .orElseThrow(() -> new StorageException(StorageErrorCode.FILE_NOT_FOUND));

        // 스토리지에서 파일 삭제
        storagePort.delete(metadata.getStorageKey());

        // 메타데이터 삭제
        saveFileMetadataPort.deleteByFileId(fileId);

        log.info("파일 삭제 완료: fileId={}", fileId);
    }

    private void validateFile(PrepareFileUploadCommand command) {
        String extension = extractExtension(command.fileName());

        if (!command.category().isAllowedExtension(extension)) {
            throw new StorageException(StorageErrorCode.INVALID_FILE_EXTENSION);
        }

        if (!command.category().isAllowedSize(command.fileSize())) {
            throw new StorageException(StorageErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    private String extractExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
}
