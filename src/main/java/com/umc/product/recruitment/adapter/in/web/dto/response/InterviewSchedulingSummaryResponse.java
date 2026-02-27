package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.InterviewSchedulingSummaryInfo;
import java.time.LocalDate;
import java.util.List;

public record InterviewSchedulingSummaryResponse(
    Progress progress,
    List<LocalDate> dateOptions,
    List<PartOptionResponse> partOptions,
    Rules rules,
    Context context
) {
    public static InterviewSchedulingSummaryResponse from(InterviewSchedulingSummaryInfo info) {
        return new InterviewSchedulingSummaryResponse(
            new Progress(info.progress().scope(), info.progress().part(), info.progress().total(),
                info.progress().scheduled()),
            info.dateOptions(),
            info.partOptions().stream().map(p -> new PartOptionResponse(p.part(), p.label(), p.done())).toList(),
            new Rules(info.rules().slotMinutes(),
                new TimeRange(info.rules().timeRange().start(), info.rules().timeRange().end())),
            new Context(info.context().date(), info.context().part())
        );
    }

    public record Progress(String scope, String part, long total, long scheduled) {
    }

    public record PartOptionResponse(String part, String label, boolean done) {
    }

    public record Rules(int slotMinutes, TimeRange timeRange) {
    }

    public record TimeRange(String start, String end) {
    }

    public record Context(String date, String part) {
    }
}
