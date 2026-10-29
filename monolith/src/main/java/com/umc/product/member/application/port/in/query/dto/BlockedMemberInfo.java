package com.umc.product.member.application.port.in.query.dto;

import java.time.Instant;

public record BlockedMemberInfo(
    Long memberId,
    String name,
    Instant blockedAt
) {
}
