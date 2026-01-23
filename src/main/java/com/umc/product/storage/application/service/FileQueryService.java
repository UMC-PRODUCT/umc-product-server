package com.umc.product.storage.application.service;

import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileInfo;
import com.umc.product.storage.application.port.out.LoadFileMetadataPort;
import com.umc.product.storage.application.port.out.StoragePort;
import com.umc.product.storage.domain.FileMetadata;
import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileQueryService implements GetFileUseCase {

    private static final long ACCESS_URL_DURATION_MINUTES = 60;

    private final StoragePort storagePort;
    private final LoadFileMetadataPort loadFileMetadataPort;

    @Override
    public FileInfo getById(String fileId) {
        FileMetadata metadata = loadFileMetadataPort.findByFileId(fileId)
                .orElseThrow(() -> new StorageException(StorageErrorCode.FILE_NOT_FOUND));

        // CDN을 통한 Signed URL 생성
        String fileLink = storagePort.generateAccessUrl(
                metadata.getStorageKey(),
                ACCESS_URL_DURATION_MINUTES
        );

        return new FileInfo(
                metadata.getFileId(),
                metadata.getOriginalFileName(),
                metadata.getCategory(),
                metadata.getContentType(),
                metadata.getFileSize(),
                fileLink,
                metadata.isUploaded(),
                metadata.getUploadedMemberId(),
                metadata.getCreatedAt()
        );
    }

    @Override
    public boolean existsById(String fileId) {
        return loadFileMetadataPort.existsByFileId(fileId);
    }
}
