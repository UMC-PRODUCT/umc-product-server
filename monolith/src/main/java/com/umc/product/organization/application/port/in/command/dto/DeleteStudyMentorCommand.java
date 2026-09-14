package com.umc.product.organization.application.port.in.command.dto;

import java.util.Objects;

public record DeleteStudyMentorCommand(
    Long groupId,
    Long mentorId
) {
    public DeleteStudyMentorCommand {
        Objects.requireNonNull(groupId, "groupId must not be null");
        Objects.requireNonNull(mentorId, "mentorId must not be null");
    }

    public static DeleteStudyMentorCommand of(Long groupId, Long mentorId) {
        return new DeleteStudyMentorCommand(groupId, mentorId);
    }
}