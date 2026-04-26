package com.umc.product.organization.application.port.in.command.dto;

import lombok.Builder;

@Builder
public record CreateStudyGroupScheduleCommand(
    Long studyGroupId,

    Long scheduleId,

    Long weeklyCurriculumId
) {
}
