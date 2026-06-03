package com.umc.product.member.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.MemberRoleType;
import com.umc.product.member.application.port.in.command.dto.UpdateMemberRoleCommand;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(
    @NotNull MemberRoleType roleType
) {
    public UpdateMemberRoleCommand toCommand(Long requesterMemberId, Long targetMemberId) {
        return UpdateMemberRoleCommand.of(requesterMemberId, targetMemberId, roleType);
    }
}
