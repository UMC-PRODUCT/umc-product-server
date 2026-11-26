package com.umc.product.inquiry.adapter.in.web.dto.response;

import java.time.Instant;

import com.umc.product.inquiry.application.port.in.query.dto.InquirySummaryInfo;
import com.umc.product.inquiry.domain.enums.InquiryStatus;
import com.umc.product.inquiry.domain.enums.InquiryTeam;

public record InquirySummaryResponse(
    Long inquiryId,
    String title,
    InquiryCategoryResponse category,
    InquiryTeam targetTeam,
    InquiryStatus status,
    Instant lastMessageAt,
    Instant createdAt
) {
    public static InquirySummaryResponse from(InquirySummaryInfo info) {
        return new InquirySummaryResponse(
            info.inquiryId(),
            info.title(),
            InquiryCategoryResponse.from(info.category()),
            info.targetTeam(),
            info.status(),
            info.lastMessageAt(),
            info.createdAt()
        );
    }
}
