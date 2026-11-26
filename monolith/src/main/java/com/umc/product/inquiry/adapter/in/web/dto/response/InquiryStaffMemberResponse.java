package com.umc.product.inquiry.adapter.in.web.dto.response;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffMemberInfo;

public record InquiryStaffMemberResponse(
    Long memberId,
    String name
) {
    public static InquiryStaffMemberResponse from(InquiryStaffMemberInfo info) {
        return info == null ? null : new InquiryStaffMemberResponse(info.memberId(), info.name());
    }
}
