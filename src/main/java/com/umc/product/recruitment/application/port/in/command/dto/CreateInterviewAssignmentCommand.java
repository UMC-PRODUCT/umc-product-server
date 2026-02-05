package com.umc.product.recruitment.application.port.in.command.dto;

public record CreateInterviewAssignmentCommand(
        Long recruitmentId,
        Long applicationId,
        Long slotId,
        Long requesterId
) {
}
