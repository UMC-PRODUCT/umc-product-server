package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.application.port.out.SaveSchedulePort;
import com.umc.product.schedule.domain.Schedule;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchedulePersistenceAdapter implements
        LoadSchedulePort,
        SaveSchedulePort {

    private final ScheduleJpaRepository scheduleJpaRepository;

    // ========== LoadSchedulePort ==========

    @Override
    public Optional<Schedule> findById(Long id) {
        return scheduleJpaRepository.findById(id);
    }

    @Override
    public List<Schedule> findAllOrderByStatusAndDate() {
        return null;
    }

    // ========== SaveSchedulePort ==========

    @Override
    public Schedule save(Schedule schedule) {
        return scheduleJpaRepository.save(schedule);
    }
}
