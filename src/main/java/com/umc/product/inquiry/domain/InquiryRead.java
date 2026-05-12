package com.umc.product.inquiry.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "inquiry_read",
    uniqueConstraints = @UniqueConstraint(columnNames = {"inquiry_id", "challenger_id"})
)
public class InquiryRead extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inquiry_id", nullable = false)
    private Long inquiryId;

    @Column(name = "challenger_id", nullable = false)
    private Long challengerId;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;

    @Builder
    private InquiryRead(Long inquiryId, Long challengerId, LocalDateTime readAt) {
        this.inquiryId = inquiryId;
        this.challengerId = challengerId;
        this.readAt = readAt;
    }

    public static InquiryRead of(Long inquiryId, Long challengerId, LocalDateTime readAt) {
        return InquiryRead.builder()
            .inquiryId(inquiryId)
            .challengerId(challengerId)
            .readAt(readAt)
            .build();
    }
}
