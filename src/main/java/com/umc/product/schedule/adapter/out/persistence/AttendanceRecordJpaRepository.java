package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.domain.AttendanceRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AttendanceRecordJpaRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByAttendanceSheetId(Long sheetId);

    void deleteAllByAttendanceSheetId(Long attendanceSheetId);

    List<AttendanceRecord> findByMemberId(Long memberId);

    Optional<AttendanceRecord> findByAttendanceSheetIdAndMemberId(Long sheetId, Long memberId);


    boolean existsByAttendanceSheetIdAndMemberId(Long sheetId, Long memberId);
}
