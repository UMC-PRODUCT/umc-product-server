package com.umc.product.storage.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.storage.domain.FileMetadata;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findById(String fileId);

    List<FileMetadata> findByIdIn(List<String> fileIds);

    boolean existsById(String fileId);

    void deleteById(String fileId);
}
