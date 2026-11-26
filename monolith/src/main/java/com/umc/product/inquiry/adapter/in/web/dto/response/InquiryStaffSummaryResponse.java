package com.umc.product.inquiry.adapter.in.web.dto.response;

import java.time.Instant;
import java.util.List;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffSummaryInfo;
import com.umc.product.inquiry.domain.enums.InquiryStatus;
import com.umc.product.inquiry.domain.enums.InquiryTeam;

import io.swagger.v3.oas.annotations.media.Schema;

public record InquiryStaffSummaryResponse(
    Long inquiryId,
    String title,
    InquiryCategoryResponse category,
    InquiryTeam targetTeam,
    InquiryStatus status,
    @Schema(description = "미배정이면 null", nullable = true)
    InquiryStaffMemberResponse assignee,
    List<InquiryLabelResponse> labels,
    Instant createdAt,
    Instant lastMessageAt
) {
    public static InquiryStaffSummaryResponse from(InquiryStaffSummaryInfo info) {
        return new InquiryStaffSummaryResponse(
            info.inquiryId(),
            info.title(),
            InquiryCategoryResponse.from(info.category()),
            info.targetTeam(),
            info.status(),
            InquiryStaffMemberResponse.from(info.assignee()),
            info.labels().stream().map(InquiryLabelResponse::from).toList(),
            info.createdAt(),
            info.lastMessageAt()
        );
    }
}
