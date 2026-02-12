package com.umc.product.recruitment.adapter.in.web.dto.request;

import com.umc.product.recruitment.application.port.in.command.dto.UpdatePublishedRecruitmentScheduleCommand;
import java.time.Instant;

public record UpdatePublishedRecruitmentScheduleRequest(
    Instant applyStartAt,
    Instant applyEndAt,
    Instant docResultAt,
    Instant interviewStartAt,
    Instant interviewEndAt,
    Instant finalResultAt,
    Integer slotMinutes
) {
    public UpdatePublishedRecruitmentScheduleCommand toCommand(Long recruitmentId, Long memberId) {
        return new UpdatePublishedRecruitmentScheduleCommand(
            memberId,
            recruitmentId,
            new UpdatePublishedRecruitmentScheduleCommand.SchedulePatch(
                applyStartAt,
                applyEndAt,
                docResultAt,
                interviewStartAt,
                interviewEndAt,
                finalResultAt
            ),
            slotMinutes
        );
    }
}
