package com.umc.product.organization.application.port.in.command.dto;

import com.umc.product.organization.domain.StudyGroupSchedule;
import lombok.Builder;

@Builder
public record CreateStudyGroupScheduleCommand(
    Long studyGroupId,

    Long scheduleId,

    Long weeklyCurriculumId
) {

    public StudyGroupSchedule toEntity() {
        return StudyGroupSchedule.builder()
            .studyGroupId(this.studyGroupId)
            .scheduleId(this.scheduleId)
            .weeklyCurriculumId(this.weeklyCurriculumId)
            .build();
    }
}
