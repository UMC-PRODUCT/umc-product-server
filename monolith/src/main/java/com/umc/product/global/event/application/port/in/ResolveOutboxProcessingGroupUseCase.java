package com.umc.product.global.event.application.port.in;

public interface ResolveOutboxProcessingGroupUseCase {

    String resolveProcessingGroup(String eventType);
}
