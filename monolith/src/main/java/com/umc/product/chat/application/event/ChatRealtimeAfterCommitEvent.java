package com.umc.product.chat.application.event;

import com.umc.product.chat.domain.event.ChatRealtimeEvent;

/**
 * 채팅 상태 변경 트랜잭션의 커밋 이후 실시간 전파를 요청한다.
 */
public record ChatRealtimeAfterCommitEvent(ChatRealtimeEvent event) {
}
