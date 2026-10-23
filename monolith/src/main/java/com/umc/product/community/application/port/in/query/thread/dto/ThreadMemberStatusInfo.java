package com.umc.product.community.application.port.in.query.thread.dto;

public record ThreadMemberStatusInfo(
    Long memberId,
    boolean isActive,
    boolean isMuted
) {
}
