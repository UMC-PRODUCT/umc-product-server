package com.umc.product.inquiry.adapter.in.web.dto.response;

import java.util.List;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryMessagePageInfo;

public record InquiryMessagePageResponse(
    List<InquiryMessageResponse> messages,
    Long nextCursor,
    boolean hasNext
) {
    public static InquiryMessagePageResponse from(InquiryMessagePageInfo info) {
        return new InquiryMessagePageResponse(
            info.messages().stream().map(InquiryMessageResponse::from).toList(),
            info.nextCursor(),
            info.hasNext()
        );
    }
}
