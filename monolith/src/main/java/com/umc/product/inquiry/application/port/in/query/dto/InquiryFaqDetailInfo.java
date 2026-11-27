package com.umc.product.inquiry.application.port.in.query.dto;

import java.time.Instant;

/**
 * 사용자가 보는 발행본.
 *
 * @param updatedAt 마지막 발행 시각
 * @param related   관련 안내. 그 대표 질문이 공개 중이 아니면 null.
 */
public record InquiryFaqDetailInfo(
    Long faqId,
    String question,
    String answer,
    InquiryCategoryInfo category,
    InquiryFaqAudienceInfo audience,
    String verifiedBy,
    Instant updatedAt,
    InquiryFaqRefInfo related
) {
}
