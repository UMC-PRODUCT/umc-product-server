package com.umc.product.inquiry.application.port.in.query.dto;

import java.time.LocalDate;
import java.util.List;

public record InquiryFaqContentInfo(
    String question,
    String answer,
    List<String> aliases,
    InquiryCategoryInfo category,
    InquiryFaqAudienceInfo audience,
    LocalDate endDate,
    InquiryFaqRefInfo related
) {
}
