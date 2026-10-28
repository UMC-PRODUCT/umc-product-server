package com.umc.product.community.adapter.in.web.dto.response;

import static com.umc.product.community.adapter.in.web.CommunityWebNumbers.text;

import java.time.Instant;

import com.umc.product.community.application.port.in.query.thread.dto.ThreadDetailInfo;
import com.umc.product.community.domain.enums.CommunityThreadCategory;
import com.umc.product.community.domain.enums.CommunityThreadMemberRole;
import com.umc.product.community.domain.enums.CommunityThreadMemberState;

import io.swagger.v3.oas.annotations.media.Schema;

public record CommunityThreadDetailResponse(
    String threadId,
    String title,
    String description,
    CommunityThreadCategory category,
    String icon,
    String memberCount,
    String unreadCount,
    String maxMembers,
    boolean isPinned,
    boolean isMuted,
    boolean isJoined,
    CommunityThreadMemberRole myRole,
    @Schema(description = "요청자의 멤버십 상태. 참여한 적 없는 스레드는 null. ACTIVE: 참여 중, LEFT: 나감, "
        + "KICKED: 강퇴됐지만 아직 확인 전(상세 조회는 허용돼 강퇴 안내를 보여줄 수 있음), "
        + "KICKED_ACKNOWLEDGED: 강퇴를 확인함(이 상태가 되면 상세 조회도 차단됨)",
        nullable = true)
    CommunityThreadMemberState membershipState,
    CommunityThreadLastMessageResponse lastMessage,
    String createdBy,
    Instant createdAt,
    Instant updatedAt,
    String shareUrl,
    Instant deletedAt
) {

    public static CommunityThreadDetailResponse from(ThreadDetailInfo info) {
        return new CommunityThreadDetailResponse(
            text(info.threadId()),
            info.title(),
            info.description(),
            info.category(),
            info.icon(),
            text(info.memberCount()),
            text(info.unreadCount()),
            text(info.maxMembers()),
            info.isPinned(),
            info.isMuted(),
            info.isJoined(),
            info.myRole(),
            info.membershipState(),
            CommunityThreadLastMessageResponse.from(info.lastMessage()),
            text(info.createdBy()),
            info.createdAt(),
            info.updatedAt(),
            info.shareUrl(),
            info.deletedAt()
        );
    }
}
