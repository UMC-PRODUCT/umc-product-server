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
@Table(name = "inquiry_message_attachment")
public class InquiryMessageAttachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inquiry_message_id", nullable = false)
    private Long inquiryMessageId;

    @Column(name = "file_metadata_id", nullable = false)
    private String fileMetadataId;

    @Builder
    private InquiryMessageAttachment(Long inquiryMessageId, String fileMetadataId) {
        this.inquiryMessageId = inquiryMessageId;
        this.fileMetadataId = fileMetadataId;
    }

    public static InquiryMessageAttachment of(Long inquiryMessageId, String fileMetadataId) {
        return InquiryMessageAttachment.builder()
            .inquiryMessageId(inquiryMessageId)
            .fileMetadataId(fileMetadataId)
            .build();
    }
}
