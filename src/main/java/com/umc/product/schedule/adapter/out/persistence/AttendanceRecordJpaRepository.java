package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface AttendanceRecordJpaRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByAttendanceSheetId(Long sheetId);

    void deleteAllByAttendanceSheetId(Long attendanceSheetId);

    List<AttendanceRecord> findByMemberId(Long memberId);

    Optional<AttendanceRecord> findByAttendanceSheetIdAndMemberId(Long sheetId, Long memberId);

    boolean existsByAttendanceSheetIdAndMemberId(Long sheetId, Long memberId);

    @Query("SELECT r FROM AttendanceRecord r WHERE r.attendanceSheetId = :sheetId " +
            "AND r.status IN (:statuses)")
    List<AttendanceRecord> findByAttendanceSheetIdAndStatusIn(
            @Param("sheetId") Long sheetId,
            @Param("statuses") List<AttendanceStatus> statuses);

    List<AttendanceRecord> findByAttendanceSheetIdIn(List<Long> sheetIds);
}
