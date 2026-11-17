package com.umc.product.storage.application.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.storage.application.port.in.command.LockStoredFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileMetadataInfo;
import com.umc.product.storage.application.port.out.LoadFileMetadataPort;
import com.umc.product.storage.domain.FileMetadata;
import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileLockService implements LockStoredFileUseCase {

    private final LoadFileMetadataPort loadFileMetadataPort;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public List<FileMetadataInfo> lockUsableByIds(List<String> fileIds, Long memberId) {
        if (fileIds == null || fileIds.isEmpty()) {
            return List.of();
        }

        Set<String> uniqueFileIds = new LinkedHashSet<>(fileIds);
        if (uniqueFileIds.stream().anyMatch(fileId -> fileId == null || fileId.isBlank())) {
            throw new StorageException(StorageErrorCode.FILE_NOT_FOUND);
        }

        List<String> orderedFileIds = uniqueFileIds.stream().sorted().toList();
        Map<String, FileMetadata> metadataById = loadFileMetadataPort
            .findByFileIdsForUpdate(orderedFileIds)
            .stream()
            .collect(Collectors.toMap(FileMetadata::getId, Function.identity()));
        if (metadataById.size() != uniqueFileIds.size()) {
            throw new StorageException(StorageErrorCode.FILE_NOT_FOUND);
        }

        return fileIds.stream()
            .distinct()
            .map(metadataById::get)
            .map(metadata -> toUsableFileInfo(metadata, memberId))
            .toList();
    }

    private FileMetadataInfo toUsableFileInfo(FileMetadata metadata, Long memberId) {
        if (metadata.getUploadedMemberId() == null
            || memberId == null
            || !Objects.equals(metadata.getUploadedMemberId(), memberId)) {
            throw new StorageException(StorageErrorCode.FILE_USE_FORBIDDEN);
        }
        if (!metadata.isUploaded()) {
            throw new StorageException(StorageErrorCode.FILE_UPLOAD_NOT_COMPLETED);
        }
        return FileMetadataInfo.from(metadata);
    }
}
