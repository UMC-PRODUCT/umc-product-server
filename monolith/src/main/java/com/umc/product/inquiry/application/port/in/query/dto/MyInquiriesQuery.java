package com.umc.product.inquiry.application.port.in.query.dto;

import com.umc.product.inquiry.domain.enums.InquiryStatusGroup;

/**
 * @param cursor 이전 페이지 마지막 문의 ID. 첫 페이지는 null.
 */
public record MyInquiriesQuery(
    Long authorMemberId,
    InquiryStatusGroup statusGroup,
    Long cursor,
    int size
) {
}
