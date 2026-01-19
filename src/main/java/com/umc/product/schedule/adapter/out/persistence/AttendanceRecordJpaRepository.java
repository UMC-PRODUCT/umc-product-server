package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.domain.AttendanceRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordJpaRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByAttendanceSheetId(Long sheetId);
}
