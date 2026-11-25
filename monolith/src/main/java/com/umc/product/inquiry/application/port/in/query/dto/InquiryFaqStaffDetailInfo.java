package com.umc.product.inquiry.application.port.in.query.dto;

import java.time.Instant;

import com.umc.product.inquiry.domain.enums.InquiryFaqStatus;

/**
 * @param published 아직 발행한 적 없으면 null.
 */
public record InquiryFaqStaffDetailInfo(
    Long faqId,
    long version,
    InquiryFaqStatus status,
    boolean hasUnpublishedChanges,
    InquiryFaqContentInfo draft,
    InquiryFaqContentInfo published,
    Instant publishedAt,
    InquiryStaffMemberInfo publishedBy,
    String verifiedBy,
    long resolvedCount
) {
}
