package com.umc.product.recruitment.adapter.out.util;

import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo.TimesByDateInfo;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
     * enabledByDate만으로 timeRange/slotMinutes를 정규화해서 disabled 계산 - slotMinutes가 0/없으면 enabled 목록에서 최소 간격 추론 (불가하면
     * default 30) - timeRange가 없으면 enabled 전체에서 min~max(+slotMinutes)로 계산
     */
    public static Normalized normalizeForApplicant(
            RecruitmentDraftInfo.DateRangeInfo dateRange, // 또는 query용 DateRangeInfo로 맞춰도 됨
            RecruitmentDraftInfo.TimeRangeInfo timeRange, // 없을 수 있음
            Integer slotMinutes,
            List<RecruitmentDraftInfo.TimesByDateInfo> enabledByDate
    ) {
        if (slotMinutes == null || slotMinutes <= 0) {
            return new Normalized(dateRange, timeRange, slotMinutes, enabledByDate, List.of());
        }

        // 1) enabled 전체에서 min/max 시간 계산
        LocalTime min = null;
        LocalTime maxExclusive = null;

        if (enabledByDate != null) {
            for (var e : enabledByDate) {
                if (e == null || e.times() == null) {
                    continue;
                }
                for (var t : e.times()) {
                    if (t == null) {
                        continue;
                    }
                    if (min == null || t.isBefore(min)) {
                        min = t;
                    }

                    // end는 "exclusive"로 맞추기 위해 slotMinutes 더한 값 기준으로 max 잡기
                    LocalTime endCandidate = t.plusMinutes(slotMinutes);
                    if (maxExclusive == null || endCandidate.isAfter(maxExclusive)) {
                        maxExclusive = endCandidate;
                    }
                }
            }
        }

        RecruitmentDraftInfo.TimeRangeInfo derivedRange =
                (min == null || maxExclusive == null || !min.isBefore(maxExclusive))
                        ? timeRange
                        : new RecruitmentDraftInfo.TimeRangeInfo(min, maxExclusive);

        // 2) dateRange는 그대로 사용(없으면 enabledByDate에서 min/max date로 계산해도 되는데 일단은 그대로)
        List<RecruitmentDraftInfo.TimesByDateInfo> disabled =
                calculateDisabled(dateRange, derivedRange, slotMinutes, enabledByDate);

        return new Normalized(dateRange, derivedRange, slotMinutes, enabledByDate, disabled);
    }

    public record Normalized(
            RecruitmentDraftInfo.DateRangeInfo dateRange,
            RecruitmentDraftInfo.TimeRangeInfo timeRange,
            Integer slotMinutes,
            List<RecruitmentDraftInfo.TimesByDateInfo> enabledByDate,
            List<RecruitmentDraftInfo.TimesByDateInfo> disabledByDate
    ) {
    }

    private static int inferSlotMinutes(List<TimesByDateInfo> enabledByDate, int defaultMinutes) {
        // enabled 중 아무 날짜나 2개 이상 있는 times를 찾아서 최소 간격 추론
        for (TimesByDateInfo e : enabledByDate) {
            List<LocalTime> times = e == null ? null : e.times();
            if (times == null || times.size() < 2) {
                continue;
            }

            List<LocalTime> sorted = times.stream().filter(Objects::nonNull).sorted().toList();
            int minDiff = Integer.MAX_VALUE;

            for (int i = 1; i < sorted.size(); i++) {
                int diff = (int) java.time.Duration.between(sorted.get(i - 1), sorted.get(i)).toMinutes();
                if (diff > 0) {
                    minDiff = Math.min(minDiff, diff);
                }
            }
            if (minDiff != Integer.MAX_VALUE) {
                return minDiff;
            }
        }
        return defaultMinutes;
    }

    private static RecruitmentDraftInfo.DateRangeInfo inferDateRange(List<TimesByDateInfo> enabledByDate) {
        LocalDate min = null, max = null;
        for (TimesByDateInfo e : enabledByDate) {
            if (e == null || e.date() == null) {
                continue;
            }
            if (min == null || e.date().isBefore(min)) {
                min = e.date();
            }
            if (max == null || e.date().isAfter(max)) {
                max = e.date();
            }
        }
        return new RecruitmentDraftInfo.DateRangeInfo(min, max);
    }

    private static RecruitmentDraftInfo.TimeRangeInfo inferTimeRangeFromEnabled(List<TimesByDateInfo> enabledByDate,
                                                                                int slotMinutes) {
        List<LocalTime> allTimes = enabledByDate.stream()
                .filter(Objects::nonNull)
                .flatMap(e -> (e.times() == null ? List.<LocalTime>of() : e.times()).stream())
                .filter(Objects::nonNull)
                .toList();

        if (allTimes.isEmpty()) {
            return new RecruitmentDraftInfo.TimeRangeInfo(null, null);
        }

        LocalTime start = allTimes.stream().min(LocalTime::compareTo).orElse(null);
        LocalTime maxStart = allTimes.stream().max(LocalTime::compareTo).orElse(null);

        // end는 마지막 시작시간 + slotMinutes
        LocalTime end = (maxStart == null) ? null : maxStart.plusMinutes(slotMinutes);

        return new RecruitmentDraftInfo.TimeRangeInfo(start, end);
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
