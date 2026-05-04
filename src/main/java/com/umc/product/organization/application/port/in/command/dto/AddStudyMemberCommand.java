package com.umc.product.organization.application.port.in.command.dto;

import java.util.Objects;

public record AddStudyMemberCommand(
    Long groupId,
    Long memberId
) {
    public AddStudyMemberCommand {
        Objects.requireNonNull(groupId, "groupId must not be null");
        Objects.requireNonNull(memberId, "memberId must not be null");
    }

    public static AddStudyMemberCommand of(Long groupId, Long memberId) {
        return new AddStudyMemberCommand(groupId, memberId);
    }
}
