package com.umc.product.curriculum.adapter.in.web.v2.dto.response;

import lombok.Builder;

@Builder
public record ChallengerWorkbookResponse(
    Long challengerWorkbookId,
    Long memberId,
    boolean isExcused,
    String excusedReason,
    String content,
    boolean hasSubmission,
    MissionSubmissionResponse submission
) {
    // 고민거리:
    // 현재 DTO는 내 커리큘럼 조회 시에도 사용됨.
    // 해당 API에서 content가 들어가면 너무 비대해지는 관계로
    // 정팩메로 content 포함 여부를 조절할 수 있도록 하고, 관련 mapper에서 알잘딱깔쎈하는거로

    // ==>
    // 이거 Info 단 DTO를 content 없는거로 두 개 만들어서 QueryDSL에서 Projection으로 해당 DTO 바로 Return 받고,
    // 그거에서 of()로 오는 정팩메 만들어야함
}
