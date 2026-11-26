package com.umc.product.inquiry.application.port.in.query.dto;

import java.time.Instant;

import com.umc.product.inquiry.domain.enums.InquiryFaqStatus;

/**
 * @param question 수정본 질문
 */
public record InquiryFaqStaffSummaryInfo(
    Long faqId,
    long version,
    InquiryFaqStatus status,
    String question,
    InquiryCategoryInfo category,
    boolean hasUnpublishedChanges,
    Instant publishedAt,
    long resolvedCount,
    Instant updatedAt
) {
}
