package com.umc.product.curriculum.adapter.in.web.v2.dto.response;

public record ChallengerWorkbookResponse(
    Long challengerWorkbookId,
    Long memberId,
    boolean isExcused,
    String excusedReason
) {
}
