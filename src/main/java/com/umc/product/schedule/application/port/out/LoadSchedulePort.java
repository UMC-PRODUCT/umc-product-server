package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LoadSchedulePort {

    Optional<Schedule> findById(Long id);

    boolean existsById(Long id);

    /**
     * @param memberId             사용자 memberId
     * @param from                 탐색을 시작할 날짜
     * @param to                   탐색을 끝낼 날짜
     * @param isAttendanceRequired true : policy가 존재하는 일정, false or null : policy가 존재하지 않는 일정
     * @return 일정 목록
     */
    List<Schedule> findMySchedules(Long memberId, Instant from, Instant to, Boolean isAttendanceRequired);

    List<Schedule> findAdminSchedules(Instant from, Instant to,
                                      AttendanceStatus attendanceStatus,
                                      Long memberId);

    Optional<Schedule> findByIdWithTags(Long scheduleId);
}
