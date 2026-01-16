package com.umc.product.member.application.port.in.command.dto;

import com.umc.product.member.domain.enums.MemberStatus;

public record UpdateMemberCommand(
        Long memberId,
        String newNickname,
        String newProfileImageId,
        MemberStatus newStatus

) {
}
