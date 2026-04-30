package com.umc.product.organization.application.port.in.command.dto;

import java.util.Objects;

public record DeleteStudyMemberCommand(
    Long groupId,
    Long memberId
) {
    public DeleteStudyMemberCommand {
        Objects.requireNonNull(groupId, "groupId must not be null");
        Objects.requireNonNull(memberId, "memberId must not be null");
    }

    public static DeleteStudyMemberCommand of(Long groupId, Long memberId) {
        return new DeleteStudyMemberCommand(groupId, memberId);
    }
}