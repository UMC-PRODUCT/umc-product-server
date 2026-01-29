package com.umc.product.storage.adapter.out.persistence;

import com.umc.product.storage.application.port.out.LoadFileMetadataPort;
import com.umc.product.storage.application.port.out.SaveFileMetadataPort;
import com.umc.product.storage.domain.FileMetadata;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileMetadataPersistenceAdapter implements LoadFileMetadataPort, SaveFileMetadataPort {

    private final FileMetadataRepository fileMetadataRepository;

    @Override
    public Optional<FileMetadata> findByFileId(String fileId) {
        return fileMetadataRepository.findByFileId(fileId);
    }

    @Override
    public boolean existsByFileId(String fileId) {
        return fileMetadataRepository.existsByFileId(fileId);
    }

    @Override
    public boolean existsById(Long id) {
        return fileMetadataRepository.existsById(id);
    }

    @Override
    public FileMetadata save(FileMetadata fileMetadata) {
        return fileMetadataRepository.save(fileMetadata);
    }

    @Override
    public void deleteByFileId(String fileId) {
        fileMetadataRepository.deleteByFileId(fileId);
    }
}
