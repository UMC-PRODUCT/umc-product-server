package com.umc.product.community.adapter.in.web.dto.response;

import static com.umc.product.community.adapter.in.web.CommunityWebNumbers.text;

import java.time.Instant;

import com.umc.product.community.application.port.in.query.thread.dto.ThreadSummaryInfo;
import com.umc.product.community.domain.enums.CommunityThreadCategory;
import com.umc.product.community.domain.enums.CommunityThreadMemberRole;
import com.umc.product.community.domain.enums.CommunityThreadMemberState;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @param membershipState 참여한 적 없는 스레드는 {@code null}. {@code KICKED}는 강퇴를 아직 확인하지
 *     않은 상태로, 클라이언트는 이 값을 보고 목록에서 비활성 처리한 뒤 강퇴 확인 API를 호출해야 한다.
 */
public record CommunityThreadSummaryResponse(
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
        + "KICKED: 강퇴됐지만 아직 확인 전(목록에 비활성으로 노출됨), "
        + "KICKED_ACKNOWLEDGED: 강퇴를 확인함(이 상태가 되면 목록 조회에서 아예 제외됨)",
        nullable = true)
    CommunityThreadMemberState membershipState,
    CommunityThreadLastMessageResponse lastMessage,
    String createdBy,
    Instant createdAt,
    Instant updatedAt
) {

    public static CommunityThreadSummaryResponse from(ThreadSummaryInfo info) {
        return new CommunityThreadSummaryResponse(
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
            info.updatedAt()
        );
    }
}
