package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.AdminScheduleInfo;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleInfo;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.Instant;
import java.util.List;

public interface GetScheduleUseCase {

    List<ScheduleInfo> searchMySchedules(Instant from,
                                         Instant to,
                                         Boolean isAttendanceRequired,
                                         Long memberId);

    ScheduleInfo getScheduleDetails(Long scheduleId, Long memberId);

    List<AdminScheduleInfo> searchAdminSchedules(Instant from, Instant to,
                                                 AttendanceStatus attendanceStatus,
                                                 Long memberId);

    AdminScheduleInfo getAdminSchedule(Long scheduleId, Long memberId,
                                       AttendanceStatus attendanceStatus);

    boolean getHaveAttendancePolicy(Long scheduleId);
}
