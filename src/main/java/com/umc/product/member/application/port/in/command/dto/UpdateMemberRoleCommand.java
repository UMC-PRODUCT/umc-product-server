package com.umc.product.member.application.port.in.command.dto;

import com.umc.product.common.domain.enums.MemberRoleType;

public record UpdateMemberRoleCommand(
    Long requesterMemberId,
    Long targetMemberId,
    MemberRoleType roleType
) {
    public static UpdateMemberRoleCommand of(Long requesterMemberId, Long targetMemberId, MemberRoleType roleType) {
        return new UpdateMemberRoleCommand(requesterMemberId, targetMemberId, roleType);
    }
}
