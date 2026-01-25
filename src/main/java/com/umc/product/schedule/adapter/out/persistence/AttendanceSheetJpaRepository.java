package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.domain.AttendanceSheet;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceSheetJpaRepository extends JpaRepository<AttendanceSheet, Long> {
    Optional<AttendanceSheet> findByScheduleId(Long scheduleId);

    List<AttendanceSheet> findByScheduleIdIn(List<Long> scheduleIds);

    void deleteByScheduleId(Long scheduleId);

    boolean existsByScheduleId(Long scheduleId);

    List<AttendanceSheet> findByActiveTrue();
}
