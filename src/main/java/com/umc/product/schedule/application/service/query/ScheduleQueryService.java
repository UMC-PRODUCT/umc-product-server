package com.umc.product.schedule.application.service.query;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.query.GetMyScheduleUseCase;
import com.umc.product.schedule.application.port.in.query.GetScheduleDetailUseCase;
import com.umc.product.schedule.application.port.in.query.GetScheduleListUseCase;
import com.umc.product.schedule.application.port.in.query.dto.MyScheduleCalendarInfo;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleDetailInfo;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleWithStatsInfo;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleQueryService implements
        GetMyScheduleUseCase,
        GetScheduleDetailUseCase {

    private final LoadSchedulePort loadSchedulePort;

    // 캘린더 나의 일정 조회하기
    @Override
    public List<MyScheduleCalendarInfo> getMyMonthlySchedules(Long memberId, int year, int month) {
        LocalDateTime now = LocalDateTime.now();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime monthStart = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime nextMonthStart = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<Schedule> schedules = loadSchedulePort.findMySchedulesByMonth(
                memberId, monthStart, nextMonthStart);

        return schedules.stream()
                .map(s -> MyScheduleCalendarInfo.of(
                        s.getId(),
                        s.getName(),
                        s.getStartsAt(),
                        s.getEndsAt(),
                        now
                ))
                .toList();
    }

    // 리스트 형식 나의 일정 조회하기 (커서 페이징)
    @Override
    public List<MyScheduleCalendarInfo> getMyMonthlyScheduleList(Long memberId, int year, int month, Long cursor,
                                                                 int size) {
        LocalDateTime now = LocalDateTime.now();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime monthStart = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime nextMonthStart = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<Schedule> schedules = loadSchedulePort.findMySchedulesByMonthWithCursor(
                memberId, monthStart, nextMonthStart, cursor, size + 1);

        return schedules.stream()
                .map(s -> MyScheduleCalendarInfo.of(
                        s.getId(),
                        s.getName(),
                        s.getStartsAt(),
                        s.getEndsAt(),
                        now
                ))
                .toList();
    }

    @Override
    public ScheduleDetailInfo getScheduleDetail(Long scheduleId) {
        LocalDateTime now = LocalDateTime.now();

        Schedule schedule = loadSchedulePort.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(
                        Domain.SCHEDULE, ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        return ScheduleDetailInfo.from(schedule, now);
    }
}
