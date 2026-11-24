package com.umc.product.inquiry.application.port.in.query.dto;

import java.util.List;

public record InquirySummaryPageInfo(
    List<InquirySummaryInfo> inquiries,
    Long nextCursor,
    boolean hasNext
) {
}
