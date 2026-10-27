package com.umc.product.global.event.application.event;

/**
 * 현재 인스턴스의 실시간 Outbox 처리를 요청하는 일시적인 신호. 영속 이벤트의 재시도는 Outbox가 담당한다.
 */
public record RealtimeOutboxCommittedEvent() {
}
