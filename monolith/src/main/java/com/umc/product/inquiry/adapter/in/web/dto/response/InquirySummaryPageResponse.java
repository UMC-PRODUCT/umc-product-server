package com.umc.product.inquiry.adapter.in.web.dto.response;

import java.util.List;

import com.umc.product.inquiry.application.port.in.query.dto.InquirySummaryPageInfo;

public record InquirySummaryPageResponse(
    List<InquirySummaryResponse> inquiries,
    Long nextCursor,
    boolean hasNext
) {
    public static InquirySummaryPageResponse from(InquirySummaryPageInfo info) {
        return new InquirySummaryPageResponse(
            info.inquiries().stream().map(InquirySummaryResponse::from).toList(),
            info.nextCursor(),
            info.hasNext()
        );
    }
}
