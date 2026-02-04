package com.umc.product.recruitment.application.port.in.query.dto;

import java.util.List;

public record InterviewSchedulingSlotsInfo(
        String date,
        String part,
        List<SlotInfo> slots
) {
    public record SlotInfo(
            Long slotId,
            String start,
            String end,
            int availableCount,
            boolean done
    ) {
    }
}