package com.umc.product.storage.adapter.out.persistence;

import com.umc.product.storage.domain.FileMetadata;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findById(String fileId);

    boolean existsById(String fileId);

    void deleteById(String fileId);
}
