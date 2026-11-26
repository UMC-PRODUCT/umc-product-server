package com.umc.product.inquiry.application.port.in.query.dto;

import java.time.Instant;

import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.inquiry.domain.enums.InquiryStatus;
import com.umc.product.inquiry.domain.enums.InquiryTeam;

public record InquirySummaryInfo(
    Long inquiryId,
    String title,
    InquiryCategoryInfo category,
    InquiryTeam targetTeam,
    InquiryStatus status,
    Instant lastMessageAt,
    Instant createdAt
) {
    public static InquirySummaryInfo of(Inquiry inquiry, InquiryCategoryInfo category) {
        return new InquirySummaryInfo(
            inquiry.getId(),
            inquiry.getTitle(),
            category,
            inquiry.getTargetTeam(),
            inquiry.getStatus(),
            inquiry.getLastMessageAt(),
            inquiry.getCreatedAt()
        );
    }
}
