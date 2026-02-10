package com.umc.product.recruitment.application.port.in.command.dto;

import com.umc.product.recruitment.application.port.in.PartOption;
import java.time.LocalDate;

public record DeleteInterviewAssignmentCommand(
    Long recruitmentId,
    Long assignmentId,
    LocalDate date,
    PartOption part,
    Long requesterId
) {
}
