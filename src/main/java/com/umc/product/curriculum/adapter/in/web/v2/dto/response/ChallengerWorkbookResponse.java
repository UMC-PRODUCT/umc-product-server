package com.umc.product.curriculum.adapter.in.web.v2.dto.response;

import lombok.Builder;

@Builder
public record ChallengerWorkbookResponse(
    Long challengerWorkbookId,
    Long memberId,
    boolean isExcused,
    String excusedReason,
    String content
) {
    // 고민거리?\:
    // 현재 DTO는 내 커리큘럼 조회 시에도 사용됨.
    // 해당 API에서 content가 들어가면 너무 비대해지는 관계로
    // 정팩메로 content 포함 여부를 조절할 수 있도록 하고, 관련 mapper에서 알잘딱깔쎈하는거로
}
