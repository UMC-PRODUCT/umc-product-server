package com.umc.product.inquiry.application.port.in.query.dto;

import java.util.List;

/**
 * 운영진이 보는 상세. 질문자용 상세에 처리 정보를 더한다.
 *
 * @param assignee      미배정이면 null.
 * @param faq           연결된 대표 질문. 없으면 미분류다.
 * @param referencedFaq 질문자가 문의 전에 본 대표 질문.
 */
public record InquiryStaffDetailInfo(
    InquiryDetailInfo detail,
    InquiryStaffMemberInfo assignee,
    List<InquiryLabelInfo> labels,
    List<InquiryHistoryInfo> histories,
    InquiryFaqRefInfo faq,
    InquiryFaqRefInfo referencedFaq
) {
}
