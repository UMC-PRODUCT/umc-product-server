package com.umc.product.schedule.application.service.query;

import com.umc.product.schedule.application.port.in.query.GetMyScheduleUseCase;
import com.umc.product.schedule.application.port.in.query.GetScheduleDetailUseCase;
import com.umc.product.schedule.application.port.in.query.dto.MyScheduleInfo;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleDetailInfo;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import com.umc.product.schedule.domain.ScheduleConstants;
import java.time.Instant;
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
    private final LoadAttendanceSheetPort loadAttendanceSheetPort;

    // 캘린더 나의 일정 조회하기
    @Override
    public List<MyScheduleInfo> getMyMonthlySchedules(Long memberId, int year, int month) {
        Instant now = Instant.now();

        YearMonth yearMonth = YearMonth.of(year, month);
        Instant monthStart = yearMonth.atDay(1).atStartOfDay(ScheduleConstants.KST).toInstant();
        Instant nextMonthStart = yearMonth.plusMonths(1).atDay(1).atStartOfDay(ScheduleConstants.KST).toInstant();

        List<Schedule> schedules = loadSchedulePort.findMySchedulesByMonth(
            memberId, monthStart, nextMonthStart);

        return schedules.stream()
            .map(s -> MyScheduleInfo.of(
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
        Instant now = Instant.now();

        Schedule schedule = loadSchedulePort.findByIdWithTags(scheduleId)
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        AttendanceSheet attendanceSheet = loadAttendanceSheetPort.findByScheduleId(scheduleId)
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND));

        return ScheduleDetailInfo.from(schedule, now, attendanceSheet.isRequiresApproval());
    }
}
