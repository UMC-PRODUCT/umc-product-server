package com.umc.product.survey.adapter.out.persistence.projection;

/**
 * 선택지별 투표자 멤버 ID 조회 결과
 */
public record OptionMemberIdProjection(
    Long optionId,
    Long memberId
) {
}
