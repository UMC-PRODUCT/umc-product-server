package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.SaveAttendanceSheetPort;
import com.umc.product.schedule.domain.AttendanceSheet;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceSheetPersistenceAdapter implements SaveAttendanceSheetPort, LoadAttendanceSheetPort {

    private final AttendanceSheetJpaRepository sheetJpaRepository;

    // ========== SaveAttendanceSheetPort ==========

    @Override
    public AttendanceSheet save(AttendanceSheet sheet) {
        return sheetJpaRepository.save(sheet);
    }

    @Override
    public void delete(AttendanceSheet sheet) {

    }

    // ========== LoadAttendanceSheetPort ==========

    @Override
    public Optional<AttendanceSheet> findById(AttendanceSheetId id) {
        return Optional.empty();
    }

    @Override
    public Optional<AttendanceSheet> findByScheduleId(Long scheduleId) {
        return sheetJpaRepository.findByScheduleId(scheduleId);
    }

    @Override
    public List<AttendanceSheet> findByScheduleIds(List<Long> scheduleIds) {
        return null;
    }

    @Override
    public boolean existsByScheduleId(Long scheduleId) {
        return false;
    }
}
