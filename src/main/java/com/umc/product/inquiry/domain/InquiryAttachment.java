package com.umc.product.inquiry.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inquiry_attachment")
public class InquiryAttachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inquiry_id", nullable = false)
    private Long inquiryId;

    @Column(name = "file_metadata_id", nullable = false)
    private String fileMetadataId;

    @Builder
    private InquiryAttachment(Long inquiryId, String fileMetadataId) {
        this.inquiryId = inquiryId;
        this.fileMetadataId = fileMetadataId;
    }

    public static InquiryAttachment of(Long inquiryId, String fileMetadataId) {
        return InquiryAttachment.builder()
            .inquiryId(inquiryId)
            .fileMetadataId(fileMetadataId)
            .build();
    }
}
