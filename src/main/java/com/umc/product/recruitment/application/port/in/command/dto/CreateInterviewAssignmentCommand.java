package com.umc.product.recruitment.application.port.in.command.dto;

import com.umc.product.recruitment.application.port.in.PartOption;
import java.time.LocalDate;

public record CreateInterviewAssignmentCommand(
    Long recruitmentId,
    LocalDate date,
    PartOption part,
    Long applicationId,
    Long slotId,
    Long requesterId
) {
}
