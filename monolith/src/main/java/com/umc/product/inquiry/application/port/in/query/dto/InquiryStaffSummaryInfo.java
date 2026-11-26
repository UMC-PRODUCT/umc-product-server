package com.umc.product.inquiry.application.port.in.query.dto;

import java.time.Instant;
import java.util.List;

import com.umc.product.inquiry.domain.enums.InquiryStatus;
import com.umc.product.inquiry.domain.enums.InquiryTeam;

/**
 * @param assignee 미배정이면 null.
 */
public record InquiryStaffSummaryInfo(
    Long inquiryId,
    String title,
    InquiryCategoryInfo category,
    InquiryTeam targetTeam,
    InquiryStatus status,
    InquiryStaffMemberInfo assignee,
    List<InquiryLabelInfo> labels,
    Instant createdAt,
    Instant lastMessageAt
) {
}
