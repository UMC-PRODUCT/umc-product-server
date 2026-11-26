package com.umc.product.inquiry.adapter.in.web.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqContentInfo;

public record InquiryFaqContentResponse(
    String question,
    String answer,
    List<String> aliases,
    InquiryCategoryResponse category,
    InquiryFaqAudienceResponse audience,
    LocalDate endDate,
    InquiryFaqRefResponse related
) {
    public static InquiryFaqContentResponse from(InquiryFaqContentInfo info) {
        if (info == null) {
            return null;
        }
        return new InquiryFaqContentResponse(
            info.question(),
            info.answer(),
            info.aliases(),
            InquiryCategoryResponse.from(info.category()),
            InquiryFaqAudienceResponse.from(info.audience()),
            info.endDate(),
            InquiryFaqRefResponse.from(info.related())
        );
    }
}
