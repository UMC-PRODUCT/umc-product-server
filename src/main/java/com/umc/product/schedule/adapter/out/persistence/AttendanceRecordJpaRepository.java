package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.domain.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordJpaRepository extends JpaRepository<AttendanceRecord, Long> {
}
