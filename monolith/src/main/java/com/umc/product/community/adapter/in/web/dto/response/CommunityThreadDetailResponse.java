package com.umc.product.community.adapter.in.web.dto.response;

import static com.umc.product.community.adapter.in.web.CommunityWebNumbers.text;

import java.time.Instant;

import com.umc.product.community.application.port.in.query.thread.dto.ThreadDetailInfo;
import com.umc.product.community.domain.enums.CommunityThreadCategory;
import com.umc.product.community.domain.enums.CommunityThreadMemberRole;
import com.umc.product.community.domain.enums.CommunityThreadMemberState;

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
