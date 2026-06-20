package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.EditMissionFeedbackCommand;

import jakarta.validation.constraints.NotBlank;

public record EditMissionFeedbackRequest(
    @NotBlank(message = "수정할 피드백 내용은 필수입니다") String content
) {
    public EditMissionFeedbackCommand toCommand(Long missionFeedbackId, Long reviewerMemberId) {
        return EditMissionFeedbackCommand.builder()
            .missionFeedbackId(missionFeedbackId)
            .reviewerMemberId(reviewerMemberId)
            .content(content)
            .build();
    }
}
