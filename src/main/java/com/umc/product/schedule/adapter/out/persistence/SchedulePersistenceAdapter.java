package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.application.port.out.DeleteSchedulePort;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.out.SaveSchedulePort;
import com.umc.product.schedule.domain.Schedule;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
    public List<Schedule> findMySchedulesByMonth(Long memberId, Instant start, Instant end) {
        return scheduleQueryRepository.findMySchedulesByMonth(
            memberId, start, end);
    }

    @Override
    public List<Schedule> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return scheduleJpaRepository.findAllById(ids);
    }

    @Override
    public List<Schedule> findAll() {
        return scheduleJpaRepository.findAll();
    }

    @Override
    public Optional<Schedule> findByIdWithTags(Long scheduleId) {
        return scheduleQueryRepository.findByIdWithTags(scheduleId);
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
