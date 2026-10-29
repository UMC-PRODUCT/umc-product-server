package com.umc.product.community.adapter.in.web.dto.response;

import static com.umc.product.community.adapter.in.web.CommunityWebNumbers.text;

import com.umc.product.community.application.port.in.command.thread.dto.CommunityThreadMemberLifecycleInfo;
import com.umc.product.community.domain.enums.CommunityThreadMemberRole;
import com.umc.product.community.domain.enums.CommunityThreadMemberState;

import io.swagger.v3.oas.annotations.media.Schema;

public record CommunityThreadMemberMutationResponse(
    String threadId,
    String memberId,
    CommunityThreadMemberRole role,
    @Schema(description = "멤버십 상태. ACTIVE: 참여 중, LEFT: 나감, "
        + "KICKED: 강퇴됐지만 아직 확인 전(목록에 비활성으로 노출됨), "
        + "KICKED_ACKNOWLEDGED: 강퇴를 확인함(목록 조회에서 제외됨)")
    CommunityThreadMemberState state,
    String memberCount
) {

    public static CommunityThreadMemberMutationResponse from(
        CommunityThreadMemberLifecycleInfo info
    ) {
        return new CommunityThreadMemberMutationResponse(
            text(info.threadId()),
            text(info.memberId()),
            info.role(),
            info.state(),
            text(info.memberCount())
        );
    }
}
