package com.umc.product.community.application.port.in.query.thread.dto;

import java.time.Instant;

import com.umc.product.community.domain.enums.CommunityThreadCategory;
import com.umc.product.community.domain.enums.CommunityThreadMemberRole;
import com.umc.product.community.domain.enums.CommunityThreadMemberState;

/**
 * @param membershipState 요청자의 멤버십 상태. 참여한 적 없는 스레드는 {@code null}이다. {@code KICKED}는
 *     강퇴를 아직 확인하지 않은 상태로, 프론트는 이 값을 보고 비활성 스레드로 표시한다.
 */
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
