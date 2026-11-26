package com.umc.product.inquiry.application.port.in.query.dto;

import com.umc.product.inquiry.domain.enums.InquiryStatusGroup;

/**
 * @param cursor 이전 응답의 nextCursor 를 그대로 받은 불투명 문자열. 첫 페이지는 null.
 */
public record MyInquiriesQuery(
    Long authorMemberId,
    InquiryStatusGroup statusGroup,
    String cursor,
    int size
) {
}
