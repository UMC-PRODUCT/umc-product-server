package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.in.query.dto.InterviewSchedulingApplicantsInfo;
import java.util.List;

public record InterviewSchedulingApplicantsResponse(
    List<AvailableApplicant> available,
    List<AlreadyScheduledApplicant> alreadyScheduled
) {
    public static InterviewSchedulingApplicantsResponse from(InterviewSchedulingApplicantsInfo info) {
        return new InterviewSchedulingApplicantsResponse(
            info.available().stream()
                .map(a -> new AvailableApplicant(
                    a.applicationId(),
                    a.nickname(),
                    a.name(),
                    toPreferredPart(a.firstPart()),
                    toPreferredPart(a.secondPart()),
                    a.documentScore()
                ))
                .toList(),
            info.alreadyScheduled().stream()
                .map(a -> new AlreadyScheduledApplicant(
                    a.applicationId(),
                    a.assignmentId(),
                    a.nickname(),
                    a.name(),
                    toPreferredPart(a.firstPart()),
                    toPreferredPart(a.secondPart()),
                    a.documentScore(),
                    new ScheduledSlot(
                        a.scheduledSlot().date(),
                        a.scheduledSlot().start(),
                        a.scheduledSlot().end()
                    )
                ))
                .toList()
        );
    }

    private static PreferredPartResponse toPreferredPart(PartOption part) {
        if (part == null) {
            return null;
        }
        return new PreferredPartResponse(part.name(), part.getLabel());
    }

    public record AvailableApplicant(
        Long applicationId,
        String nickname,
        String name,
        PreferredPartResponse firstPart,
        PreferredPartResponse secondPart,
        double documentScore
    ) {
    }

    public record AlreadyScheduledApplicant(
        Long applicationId,
        Long assignmentId,
        String nickname,
        String name,
        PreferredPartResponse firstPart,
        PreferredPartResponse secondPart,
        double documentScore,
        ScheduledSlot scheduledSlot
    ) {
    }

    public record PreferredPartResponse(String part, String label) {
    }

    public record ScheduledSlot(
        String date,
        String start,
        String end
    ) {
    }
}
