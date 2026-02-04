package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.InterviewSchedulingSlotsInfo;
import java.util.List;

public record InterviewSchedulingSlotsResponse(
        String date,
        String part,
        List<SlotResponse> slots
) {
    public static InterviewSchedulingSlotsResponse from(InterviewSchedulingSlotsInfo info) {
        return new InterviewSchedulingSlotsResponse(
                info.date(),
                info.part(),
                info.slots().stream()
                        .map(s -> new SlotResponse(s.slotId(), s.start(), s.end(), s.availableCount(), s.done()))
                        .toList()
        );
    }

    public record SlotResponse(
            Long slotId,
            String start,
            String end,
            int availableCount,
            boolean done
    ) {
    }
}