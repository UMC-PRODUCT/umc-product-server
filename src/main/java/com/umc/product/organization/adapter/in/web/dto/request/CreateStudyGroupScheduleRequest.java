package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.command.dto.CreateStudyGroupScheduleCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "스터디 그룹 일정 생성 요청")
public record CreateStudyGroupScheduleRequest(
    @Schema(description = "스터디 그룹 ID", example = "1")
    @NotNull(message = "스터디 그룹 ID는 필수입니다")
    Long studyGroupId,

    @Schema(description = "일정 ID", example = "1")
    @NotNull(message = "일정 ID는 필수입니다")
    Long scheduleId,

    @Schema(description = "주간 커리큘럼 ID", example = "1")
    @NotNull(message = "주간 커리큘럼 ID는 필수입니다")
    Long weeklyCurriculumId
) {

    public CreateStudyGroupScheduleCommand toCommand() {
        return CreateStudyGroupScheduleCommand.builder()
            .studyGroupId(studyGroupId)
            .scheduleId(scheduleId)
            .weeklyCurriculumId(weeklyCurriculumId)
            .build();
    }
}
