package com.umc.product.support.fixture;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.umc.product.inquiry.adapter.out.persistence.InquiryCategoryJpaRepository;
import com.umc.product.inquiry.adapter.out.persistence.InquiryFaqAudienceJpaRepository;
import com.umc.product.inquiry.application.port.out.LoadInquiryLabelPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryLabelPort;
import com.umc.product.inquiry.domain.InquiryCategory;
import com.umc.product.inquiry.domain.InquiryFaqAudience;
import com.umc.product.inquiry.domain.InquiryLabel;
import com.umc.product.storage.application.port.out.SaveFileMetadataPort;
import com.umc.product.storage.domain.FileMetadata;
import com.umc.product.storage.domain.enums.FileCategory;
import com.umc.product.storage.domain.enums.StorageProvider;

@Component
public class InquiryFixture extends FixtureSupport {

    private final InquiryCategoryJpaRepository inquiryCategoryJpaRepository;
    private final SaveFileMetadataPort saveFileMetadataPort;
    private final LoadInquiryLabelPort loadInquiryLabelPort;
    private final SaveInquiryLabelPort saveInquiryLabelPort;
    private final InquiryFaqAudienceJpaRepository inquiryFaqAudienceJpaRepository;

    public InquiryFixture(
        InquiryCategoryJpaRepository inquiryCategoryJpaRepository,
        SaveFileMetadataPort saveFileMetadataPort,
        LoadInquiryLabelPort loadInquiryLabelPort,
        SaveInquiryLabelPort saveInquiryLabelPort,
        InquiryFaqAudienceJpaRepository inquiryFaqAudienceJpaRepository
    ) {
        this.inquiryFaqAudienceJpaRepository = inquiryFaqAudienceJpaRepository;
        this.inquiryCategoryJpaRepository = inquiryCategoryJpaRepository;
        this.saveFileMetadataPort = saveFileMetadataPort;
        this.loadInquiryLabelPort = loadInquiryLabelPort;
        this.saveInquiryLabelPort = saveInquiryLabelPort;
    }

    public InquiryFaqAudience 대상(String name) {
        return inquiryFaqAudienceJpaRepository.findAll().stream()
            .filter(audience -> audience.getName().equals(name))
            .findFirst()
            .orElseGet(() -> inquiryFaqAudienceJpaRepository.save(InquiryFaqAudience.create(name, 1)));
    }

    public InquiryLabel 라벨(String name) {
        return loadInquiryLabelPort.listAll().stream()
            .filter(label -> label.getName().equals(name))
            .findFirst()
            .orElseGet(() -> saveInquiryLabelPort.save(InquiryLabel.create(name)));
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
