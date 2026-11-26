package com.umc.product.community.application.port.out.thread;

import com.umc.product.community.domain.event.CommunityThreadLifecycleEvent.RealtimeOnly;

public interface PublishCommunityThreadRealtimePort {

    void publishAfterCommit(RealtimeOnly event);
}
