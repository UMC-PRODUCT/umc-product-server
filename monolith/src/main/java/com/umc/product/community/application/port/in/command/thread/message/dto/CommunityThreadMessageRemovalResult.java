package com.umc.product.community.application.port.in.command.thread.message.dto;

/**
 * 운영 조치로 스레드 메시지를 삭제한 결과입니다.
 */
public enum CommunityThreadMessageRemovalResult {
    DELETED,
    ALREADY_DELETED,
    NOT_FOUND
}
