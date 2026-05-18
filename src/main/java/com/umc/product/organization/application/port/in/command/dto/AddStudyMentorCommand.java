package com.umc.product.organization.application.port.in.command.dto;

import java.util.Objects;

public record AddStudyMentorCommand(
    Long groupId,
    Long mentorId
) {
    public AddStudyMentorCommand {
        Objects.requireNonNull(groupId, "groupId must not be null");
        Objects.requireNonNull(mentorId, "mentorId must not be null");
    }

    public static AddStudyMentorCommand of(Long groupId, Long mentorId) {
        return new AddStudyMentorCommand(groupId, mentorId);
    }
}