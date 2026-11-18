package com.umc.product.chat.application.event;

import com.umc.product.chat.domain.event.ChatMessageCreatedEvent;

/**
 * 메시지 생성 트랜잭션의 커밋 이후 실시간 전송을 요청한다.
 */
public record ChatMessageCreatedAfterCommitEvent(ChatMessageCreatedEvent messageCreated) {
}
