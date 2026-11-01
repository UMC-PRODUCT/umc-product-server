package com.umc.product.member.adapter.in.web.dto.response;

import com.umc.product.member.application.port.in.command.dto.MemberBlockInfo;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberBlockResponse(
    Long memberId,
    @Schema(description = "처리 후 차단 상태. 차단 완료면 true, 차단 해제 완료면 false")
    boolean isBlocked
) {

    public static MemberBlockResponse from(MemberBlockInfo info) {
        return new MemberBlockResponse(info.blockedMemberId(), info.blocked());
    }
}
