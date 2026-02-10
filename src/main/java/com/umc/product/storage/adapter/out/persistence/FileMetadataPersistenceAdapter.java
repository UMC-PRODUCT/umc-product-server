package com.umc.product.storage.adapter.out.persistence;

import com.umc.product.storage.application.port.out.LoadFileMetadataPort;
import com.umc.product.storage.application.port.out.SaveFileMetadataPort;
import com.umc.product.storage.domain.FileMetadata;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileMetadataPersistenceAdapter implements LoadFileMetadataPort, SaveFileMetadataPort {

    private final FileMetadataRepository fileMetadataRepository;

    @Override
    public Optional<FileMetadata> findByFileId(String fileId) {
        return fileMetadataRepository.findById(fileId);
    }

    @Override
    public List<FileMetadata> findByFileIds(List<String> fileIds) {
        return fileMetadataRepository.findByIdIn(fileIds);
    }

    @Override
    public boolean existsByFileId(String fileId) {
        return fileMetadataRepository.existsById(fileId);
    }

    @Override
    public FileMetadata save(FileMetadata fileMetadata) {
        return fileMetadataRepository.save(fileMetadata);
    }

    @Override
    public void deleteByFileId(String fileId) {
        fileMetadataRepository.deleteById(fileId);
    }
}
