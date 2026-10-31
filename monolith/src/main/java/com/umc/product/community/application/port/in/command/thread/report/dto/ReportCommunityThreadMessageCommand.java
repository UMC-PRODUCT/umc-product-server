package com.umc.product.community.application.port.in.command.thread.report.dto;

import java.util.List;

import com.umc.product.report.domain.enums.ReportReason;

public record ReportCommunityThreadMessageCommand(
    Long messageId,
    Long requesterMemberId,
    List<ReportReason> reasons
) {

    public ReportCommunityThreadMessageCommand {
        messageId = requirePositive(messageId, "messageId");
        requesterMemberId = requirePositive(requesterMemberId, "requesterMemberId");
        if (reasons == null || reasons.isEmpty()) {
            throw new IllegalArgumentException("reasons must not be empty");
        }
        reasons = List.copyOf(reasons);
    }

    private static Long requirePositive(Long value, String name) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }
}
