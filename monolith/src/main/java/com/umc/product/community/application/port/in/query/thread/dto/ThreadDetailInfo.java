package com.umc.product.community.application.port.in.query.thread.dto;

import java.time.Instant;

import com.umc.product.community.domain.enums.CommunityThreadCategory;
import com.umc.product.community.domain.enums.CommunityThreadMemberRole;
import com.umc.product.community.domain.enums.CommunityThreadMemberState;

public record ThreadDetailInfo(
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
    Instant updatedAt,
    String shareUrl,
    Instant deletedAt
) {

    public static ThreadDetailInfo from(ThreadSummaryInfo summary, String shareUrl, Instant deletedAt) {
        return new ThreadDetailInfo(
            summary.threadId(),
            summary.title(),
            summary.description(),
            summary.category(),
            summary.icon(),
            summary.memberCount(),
            summary.unreadCount(),
            summary.maxMembers(),
            summary.isPinned(),
            summary.isMuted(),
            summary.isJoined(),
            summary.myRole(),
            summary.membershipState(),
            summary.lastMessage(),
            summary.createdBy(),
            summary.createdAt(),
            summary.updatedAt(),
            shareUrl,
            deletedAt
        );
    }
}
