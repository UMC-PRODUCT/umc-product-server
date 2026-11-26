package com.umc.product.inquiry.application.port.in.command.dto;

import java.util.Set;

public record ReplaceInquiryLabelsCommand(
    Long inquiryId,
    Long actorMemberId,
    Set<Long> labelIds
) {
    public ReplaceInquiryLabelsCommand {
        labelIds = labelIds == null ? Set.of() : Set.copyOf(labelIds);
    }
}
