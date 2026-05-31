package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.application.port.out.DeleteSchedulePort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.out.SaveSchedulePort;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchedulePersistenceAdapter implements
    LoadSchedulePort,
    SaveSchedulePort,
    DeleteSchedulePort {

    private final ScheduleJpaRepository scheduleJpaRepository;
    private final ScheduleQueryRepository scheduleQueryRepository;

    // ========== LoadSchedulePort ==========

    @Override
    public Optional<Schedule> findById(Long id) {
        return scheduleJpaRepository.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return scheduleJpaRepository.existsById(id);
    }

    @Override
    public List<Schedule> findMySchedules(Long memberId, Instant from, Instant to, Boolean isAttendanceRequired) {
        return scheduleQueryRepository.findMySchedules(memberId, from, to, isAttendanceRequired);
    }

    @Override
    public Optional<Schedule> findByIdWithTags(Long scheduleId) {
        return scheduleQueryRepository.findByIdWithTags(scheduleId);
    }

    @Override
    public List<Schedule> findAdminSchedulesByRole(Set<Long> targetScheduleIds,
                                                   Instant from,
                                                   Instant to,
                                                   AttendanceStatus attendanceStatus) {
        return scheduleQueryRepository.findAdminSchedulesByRole(targetScheduleIds, from, to, attendanceStatus);
    }

    @Override
    public Set<Long> findScheduleIdsByAuthor(Long authorMemberId) {
        return scheduleQueryRepository.findScheduleIdsByAuthor(authorMemberId);
    }

    // ========== SaveSchedulePort ==========

    @Override
    public Schedule save(Schedule schedule) {
        return scheduleJpaRepository.save(schedule);
    }

    // ========== DeleteSchedulePort ==========
    @Override
    public void delete(Long scheduleId) {
        scheduleJpaRepository.deleteById(scheduleId);
    }
}
