package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import com.umc.product.curriculum.application.port.in.command.dto.workbook.CreateWeeklyBestWorkbookCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBestWorkbookRequest(
    @NotNull(message = "베스트 워크북 대상 멤버 ID는 필수입니다.") Long bestMemberId,

    @NotNull(message = "주차별 커리큘럼 ID는 필수입니다.") Long weeklyCurriculumId,

    @NotNull(message = "스터디 그룹 ID는 필수입니다.") Long studyGroupId, // NOT NULL!

    @NotBlank(message = "선정 사유는 필수입니다.") String reason
) {

    public CreateWeeklyBestWorkbookCommand toCommand(Long decidedMemberId) {
        return CreateWeeklyBestWorkbookCommand.builder()
            .decidedMemberId(decidedMemberId)
            .bestMemberId(bestMemberId)
            .weeklyCurriculumId(weeklyCurriculumId)
            .studyGroupId(studyGroupId)
            .reason(reason)
            .build();
    }
}
