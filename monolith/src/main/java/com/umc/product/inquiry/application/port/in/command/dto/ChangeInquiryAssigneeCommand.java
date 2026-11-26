package com.umc.product.inquiry.application.port.in.command.dto;

/**
 * @param assigneeMemberId         새 담당자. null이면 담당자를 비운다.
 * @param expectedAssigneeMemberId 요청자가 화면에서 본 지금 담당자. 미배정이었으면 null.
 */
public record ChangeInquiryAssigneeCommand(
    Long inquiryId,
    Long actorMemberId,
    Long assigneeMemberId,
    Long expectedAssigneeMemberId
) {
}
