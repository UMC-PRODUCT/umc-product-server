package com.umc.product.inquiry.application.port.in.query.dto;

import java.util.List;

/**
 * @param counts 탭별 개수. 탭 말고 다른 필터는 똑같이 적용한다.
 */
public record InquiryStaffPageInfo(
    TabCounts counts,
    List<InquiryStaffSummaryInfo> inquiries,
    String nextCursor,
    boolean hasNext
) {
    public record TabCounts(long all, long mine, long unassigned) {
    }
}
