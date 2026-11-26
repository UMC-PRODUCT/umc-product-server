package com.umc.product.inquiry.adapter.in.web.dto.response;

import java.util.List;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffPageInfo;

import io.swagger.v3.oas.annotations.media.Schema;

public record InquiryStaffPageResponse(
    @Schema(description = "탭별 개수. 탭 말고 다른 필터는 똑같이 적용한다. 첫 페이지에만 있고 cursor를 넘긴 요청에서는 null", nullable = true)
    TabCounts counts,
    List<InquiryStaffSummaryResponse> inquiries,
    String nextCursor,
    boolean hasNext
) {
    public static InquiryStaffPageResponse from(InquiryStaffPageInfo info) {
        return new InquiryStaffPageResponse(
            info.counts() == null
                ? null
                : new TabCounts(info.counts().all(), info.counts().mine(), info.counts().unassigned()),
            info.inquiries().stream().map(InquiryStaffSummaryResponse::from).toList(),
            info.nextCursor(),
            info.hasNext()
        );
    }

    public record TabCounts(long all, long mine, long unassigned) {
    }
}
