package com.umc.product.recruitment.application.port.in.query.dto;

import java.time.LocalDate;
import java.util.List;

public record InterviewSchedulingSummaryInfo(
        ProgressInfo progress,
        List<LocalDate> dateOptions,
        List<PartOptionInfo> partOptions,
        RulesInfo rules,
        ContextInfo context
) {
    public record ProgressInfo(String scope, String part, int total, int scheduled) {
    }

    public record PartOptionInfo(String part, String label, boolean done) {
    }

    public record RulesInfo(int slotMinutes, TimeRangeInfo timeRange) {
    }

    public record TimeRangeInfo(String start, String end) {
    } // HH:mm

    public record ContextInfo(String date, String part) {
    }   // date: YYYY-MM-DD
}
