package com.umc.product.inquiry.application.port.in.query.dto;

public record InquiryAuthorInfo(
    Long memberId,
    String name,
    String nickname,
    String profileImageUrl
) {
}
