package com.umc.product.survey.adapter.out.persistence.projection;

/**
 * 선택지별 득표수 집계 결과
 */
public record OptionVoteCountProjection(
    Long optionId,
    Long voteCount
) {
}
