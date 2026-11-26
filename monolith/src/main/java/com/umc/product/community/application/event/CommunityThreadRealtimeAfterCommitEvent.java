package com.umc.product.community.application.event;

import com.umc.product.community.domain.event.CommunityThreadLifecycleEvent.RealtimeOnly;

public record CommunityThreadRealtimeAfterCommitEvent(RealtimeOnly event) {
}
