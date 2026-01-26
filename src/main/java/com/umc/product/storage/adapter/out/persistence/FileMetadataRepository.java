package com.umc.product.storage.adapter.out.persistence;

import com.umc.product.storage.domain.FileMetadata;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findByFileId(String fileId);

    boolean existsByFileId(String fileId);

    void deleteByFileId(String fileId);
}
