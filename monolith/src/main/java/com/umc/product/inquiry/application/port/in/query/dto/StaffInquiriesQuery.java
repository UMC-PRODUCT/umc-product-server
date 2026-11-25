package com.umc.product.inquiry.application.port.in.query.dto;

import java.util.Set;

import com.umc.product.inquiry.domain.enums.InquiryStaffTab;
import com.umc.product.inquiry.domain.enums.InquiryStatus;

/**
 * @param labelIds         하나라도 붙은 문의만 고른다.
 * @param unclassifiedOnly 대표 질문에 연결되지 않은 문의만 고른다.
 * @param cursor   이전 응답의 nextCursor 를 그대로 받은 불투명 문자열. 첫 페이지는 null.
 */
public record StaffInquiriesQuery(
    Long staffMemberId,
    InquiryStaffTab tab,
    Set<InquiryStatus> statuses,
    Set<Long> categoryIds,
    Set<Long> labelIds,
    boolean unclassifiedOnly,
    String cursor,
    int size
) {
    public StaffInquiriesQuery {
        statuses = statuses == null ? Set.of() : Set.copyOf(statuses);
        categoryIds = categoryIds == null ? Set.of() : Set.copyOf(categoryIds);
        labelIds = labelIds == null ? Set.of() : Set.copyOf(labelIds);
    }
}
