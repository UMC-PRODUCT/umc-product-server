package com.umc.product.community.application.port.in.query.thread.dto;

import java.time.Instant;

import com.umc.product.community.domain.enums.CommunityThreadCategory;
import com.umc.product.community.domain.enums.CommunityThreadMemberRole;
import com.umc.product.community.domain.enums.CommunityThreadMemberState;

public record ThreadSummaryInfo(
    Long threadId,
    String title,
    String description,
    CommunityThreadCategory category,
    String icon,
    long memberCount,
    long unreadCount,
    int maxMembers,
    boolean isPinned,
    boolean isMuted,
    boolean isJoined,
    CommunityThreadMemberRole myRole,
    CommunityThreadMemberState membershipState,
    ThreadLastMessageInfo lastMessage,
    Long createdBy,
    Instant createdAt,
    Instant updatedAt
) {
}
