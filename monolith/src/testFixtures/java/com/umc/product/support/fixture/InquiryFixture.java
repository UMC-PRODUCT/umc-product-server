package com.umc.product.support.fixture;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.umc.product.inquiry.adapter.out.persistence.InquiryCategoryJpaRepository;
import com.umc.product.inquiry.domain.InquiryCategory;
import com.umc.product.storage.application.port.out.SaveFileMetadataPort;
import com.umc.product.storage.domain.FileMetadata;
import com.umc.product.storage.domain.enums.FileCategory;
import com.umc.product.storage.domain.enums.StorageProvider;

@Component
public class InquiryFixture extends FixtureSupport {

    private final InquiryCategoryJpaRepository inquiryCategoryJpaRepository;
    private final SaveFileMetadataPort saveFileMetadataPort;

    public InquiryFixture(
        InquiryCategoryJpaRepository inquiryCategoryJpaRepository,
        SaveFileMetadataPort saveFileMetadataPort
    ) {
        this.inquiryCategoryJpaRepository = inquiryCategoryJpaRepository;
        this.saveFileMetadataPort = saveFileMetadataPort;
    }

    public InquiryCategory 카테고리(String name, int displayOrder) {
        return inquiryCategoryJpaRepository.findAll().stream()
            .filter(category -> category.getName().equals(name))
            .findFirst()
            .orElseGet(() -> inquiryCategoryJpaRepository.save(InquiryCategory.create(name, displayOrder)));
    }

    public FileMetadata 업로드된_파일(Long uploaderMemberId, FileCategory category) {
        return 업로드된_파일(uploaderMemberId, category, "screenshot.png", "image/png");
    }

    public FileMetadata 업로드된_파일(
        Long uploaderMemberId,
        FileCategory category,
        String originalFileName,
        String contentType
    ) {
        String fileId = UUID.randomUUID().toString();
        FileMetadata metadata = FileMetadata.builder()
            .fileId(fileId)
            .originalFileName(originalFileName)
            .category(category)
            .contentType(contentType)
            .fileSize(1024L)
            .storageProvider(StorageProvider.AWS_S3)
            .storageKey(category.getPathPrefix() + "/" + fileId + "-" + originalFileName)
            .uploadedMemberId(uploaderMemberId)
            .build();
        metadata.markAsUploaded();
        return saveFileMetadataPort.save(metadata);
    }
}
