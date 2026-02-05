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
import java.time.Instant;
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
    public List<MyScheduleInfo> getMyMonthlySchedules(Long memberId, Instant from, Instant to) {
        List<Schedule> schedules = loadSchedulePort.findMySchedulesByMonth(memberId, from, to);

        return schedules.stream()
            .map(s -> MyScheduleInfo.of(
                s.getId(),
                s.getName(),
                s.getStartsAt(),
                s.getEndsAt(),
                Instant.now()
            ))
            .toList();
    }

    @Override
    public ScheduleDetailInfo getScheduleDetail(Long scheduleId) {
        Schedule schedule = loadSchedulePort.findByIdWithTags(scheduleId)
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        AttendanceSheet attendanceSheet = loadAttendanceSheetPort.findByScheduleId(scheduleId)
            .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.ATTENDANCE_SHEET_NOT_FOUND));

        return ScheduleDetailInfo.from(schedule, Instant.now(), attendanceSheet.isRequiresApproval());
    }
}
