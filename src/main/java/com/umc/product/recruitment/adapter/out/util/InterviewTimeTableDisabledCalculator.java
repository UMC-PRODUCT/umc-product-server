package com.umc.product.recruitment.adapter.out.util;

import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo.TimesByDateInfo;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class InterviewTimeTableDisabledCalculator {

    private InterviewTimeTableDisabledCalculator() {
    }

    public static List<TimesByDateInfo> calculateDisabled(
            RecruitmentDraftInfo.DateRangeInfo dateRange,
            RecruitmentDraftInfo.TimeRangeInfo timeRange,
            Integer slotMinutes,
            List<RecruitmentDraftInfo.TimesByDateInfo> enabledByDate
    ) {
        if (dateRange == null || timeRange == null || slotMinutes == null || slotMinutes <= 0) {
            return List.of();
        }

        LocalDate start = dateRange.start();
        LocalDate end = dateRange.end();
        if (start == null || end == null || start.isAfter(end)) {
            return List.of();
        }

        List<TimesByDateInfo> result = new ArrayList<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            final LocalDate targetDate = date;

            final List<LocalTime> all = generateSlots(timeRange.start(), timeRange.end(), slotMinutes);

            final List<LocalTime> enabled =
                    enabledByDate == null ? List.of()
                            : enabledByDate.stream()
                                    .filter(e -> targetDate.equals(e.date()))
                                    .findFirst()
                                    .map(TimesByDateInfo::times)
                                    .orElse(List.of());

            final List<LocalTime> disabled = all.stream()
                    .filter(t -> !enabled.contains(t))
                    .toList();

            result.add(new TimesByDateInfo(targetDate, disabled));
        }

        return result;
    }

    /**
     * start inclusive, end exclusive 예: 10:00~12:00, 30분 -> 10:00,10:30,11:00,11:30
     */
    private static List<LocalTime> generateSlots(LocalTime start, LocalTime end, int minutes) {
        if (start == null || end == null || !start.isBefore(end)) {
            return List.of();
        }

        List<LocalTime> slots = new ArrayList<>();
        LocalTime t = start;
        while (t.isBefore(end)) {
            slots.add(t);
            t = t.plusMinutes(minutes);
        }
        return slots;
    }
}
