package com.umc.product.member.adapter.in.web.dto.response;

import java.time.Instant;

import com.umc.product.member.application.port.in.query.dto.BlockedMemberInfo;

public record BlockedMemberResponse(
    Long memberId,
    String name,
    Instant blockedAt
) {

    public static BlockedMemberResponse from(BlockedMemberInfo info) {
        return new BlockedMemberResponse(info.memberId(), info.name(), info.blockedAt());
    }
}
