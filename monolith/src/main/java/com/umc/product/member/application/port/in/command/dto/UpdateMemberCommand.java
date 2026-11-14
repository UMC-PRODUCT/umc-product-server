package com.umc.product.member.application.port.in.command.dto;

public record UpdateMemberCommand(
    Long memberId,
    String newProfileImageId

) {
    public static UpdateMemberCommand forProfileUpdate(
        Long memberId,
        String newProfileImageId
    ) {
        return new UpdateMemberCommand(memberId, newProfileImageId);
    }
}
