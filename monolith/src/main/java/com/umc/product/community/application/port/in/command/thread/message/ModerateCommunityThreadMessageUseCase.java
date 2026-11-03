package com.umc.product.community.application.port.in.command.thread.message;

import com.umc.product.community.application.port.in.command.thread.message.dto.CommunityThreadMessageRemovalResult;

/**
 * 운영 조치로 스레드 메시지를 삭제합니다.
 * <p>
 * 요청자가 스레드 멤버가 아닌 운영 조치용이며, 사용자 요청 경로에서는 쓰지 않습니다.
 */
public interface ModerateCommunityThreadMessageUseCase {

    /**
     * 스레드 방장 권한으로 메시지를 삭제합니다. 스레드가 없거나 삭제되었으면 {@link CommunityThreadMessageRemovalResult#NOT_FOUND}를
     * 돌려줍니다.
     */
    CommunityThreadMessageRemovalResult removeByOperator(Long threadId, Long messageId);
}
