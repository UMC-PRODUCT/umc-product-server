package com.umc.product.recruitment.adapter.in.web.dto.response;

import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentScheduleInfo;
import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

public record RecruitmentSchedulesResponse(
        Long recruitmentId,
        List<ScheduleItemResponse> schedules
) {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    public static RecruitmentSchedulesResponse from(RecruitmentScheduleInfo info) {
        List<RecruitmentScheduleInfo.ScheduleItem> source =
                info.schedules() == null ? List.of() : info.schedules();

        List<ScheduleItemResponse> items = source.stream()
                .filter(schedule -> schedule.startsAt() != null)
                .sorted(Comparator.comparing(RecruitmentScheduleInfo.ScheduleItem::startsAt))
                .map(ScheduleItemResponse::from)
                .toList();

        return new RecruitmentSchedulesResponse(info.recruitmentId(), items);
    }

    public record ScheduleItemResponse(
            RecruitmentScheduleType type,
            RecruitmentScheduleInfo.ScheduleKind kind,
            LocalDate startDate,
            LocalDate endDate
    ) {
        public static ScheduleItemResponse from(RecruitmentScheduleInfo.ScheduleItem item) {
            LocalDate start = item.startsAt() == null ? null : item.startsAt().atZone(ZONE_ID).toLocalDate();
            LocalDate end = item.endsAt() == null ? null : item.endsAt().atZone(ZONE_ID).toLocalDate();

            // kind가 AT면 endDate를 startDate로 맞춰서 프론트가 더 단순해짐(데이터 일관성)
            if (item.kind() == RecruitmentScheduleInfo.ScheduleKind.AT) {
                end = start;
            }

            return new ScheduleItemResponse(item.type(), item.kind(), start, end);
        }
    }
}
