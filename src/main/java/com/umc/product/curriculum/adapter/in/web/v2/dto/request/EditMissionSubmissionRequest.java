package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.EditMissionSubmissionCommand;

import jakarta.validation.constraints.NotBlank;

public record EditMissionSubmissionRequest(
    @NotBlank(message = "수정할 제출 내용은 필수입니다") String content
) {
    public EditMissionSubmissionCommand toCommand(Long missionSubmissionId, Long requesterMemberId) {
        return EditMissionSubmissionCommand.builder()
            .missionSubmissionId(missionSubmissionId)
            .requesterMemberId(requesterMemberId)
            .content(content)
            .build();
    }
}
