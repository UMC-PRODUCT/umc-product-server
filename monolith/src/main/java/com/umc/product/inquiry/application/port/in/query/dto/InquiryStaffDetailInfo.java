package com.umc.product.inquiry.application.port.in.query.dto;

import java.util.List;

/**
 * 운영진이 보는 상세. 질문자용 상세에 처리 정보를 더한다.
 *
 * @param assignee       미배정이면 null.
 * @param canBeAssignee  보는 사람이 담당자가 될 수 있는지. {@code SUPER_ADMIN}만 가진 회원은 문의를 처리할 수 있어도 false다.
 * @param faq            연결된 대표 질문. 없으면 미분류다.
 * @param referencedFaq  질문자가 문의 전에 본 대표 질문.
 */
public record InquiryStaffDetailInfo(
    InquiryDetailInfo detail,
    InquiryStaffMemberInfo assignee,
    boolean canBeAssignee,
    List<InquiryLabelInfo> labels,
    List<InquiryHistoryInfo> histories,
    InquiryFaqRefInfo faq,
    InquiryFaqRefInfo referencedFaq
) {
}
