package com.umc.product.member.adapter.in.web.dto.response;

import java.util.List;

import com.umc.product.member.application.port.in.query.dto.BlockedMemberInfo;

public record BlockedMemberListResponse(
    List<BlockedMemberResponse> members
) {

    public static BlockedMemberListResponse from(List<BlockedMemberInfo> infos) {
        return new BlockedMemberListResponse(infos.stream().map(BlockedMemberResponse::from).toList());
    }
}
