package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.domain.AttendanceSheet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceSheetJpaRepository extends JpaRepository<AttendanceSheet, Long> {
}
