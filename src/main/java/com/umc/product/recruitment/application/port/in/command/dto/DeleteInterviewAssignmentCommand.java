package com.umc.product.recruitment.application.port.in.command.dto;

public record DeleteInterviewAssignmentCommand(
    Long recruitmentId,
    Long assignmentId,
    Long requesterId
) {
}
