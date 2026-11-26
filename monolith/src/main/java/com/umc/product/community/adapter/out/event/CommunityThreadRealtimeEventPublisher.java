package com.umc.product.community.adapter.out.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.umc.product.community.application.event.CommunityThreadRealtimeAfterCommitEvent;
import com.umc.product.community.application.port.out.thread.PublishCommunityThreadRealtimePort;
import com.umc.product.community.domain.event.CommunityThreadLifecycleEvent.RealtimeOnly;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class CommunityThreadRealtimeEventPublisher implements PublishCommunityThreadRealtimePort {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishAfterCommit(RealtimeOnly event) {
        eventPublisher.publishEvent(new CommunityThreadRealtimeAfterCommitEvent(event));
    }
}
