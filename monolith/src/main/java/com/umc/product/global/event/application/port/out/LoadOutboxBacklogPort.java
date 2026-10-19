package com.umc.product.global.event.application.port.out;

public interface LoadOutboxBacklogPort {

    OutboxBacklog getBacklog(OutboxEventSelection selection);

    record OutboxBacklog(long pending, long processing, double oldestAgeSeconds) {
    }
}
