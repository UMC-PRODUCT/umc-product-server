package com.umc.product.global.event.domain;

public enum EventOutboxStatus {
    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED
}
