package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.application.port.out.DeleteAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceSheetPort;
import com.umc.product.schedule.application.port.out.SaveAttendanceSheetPort;
import com.umc.product.schedule.domain.AttendanceSheet;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceSheetPersistenceAdapter implements SaveAttendanceSheetPort, LoadAttendanceSheetPort,
    DeleteAttendanceSheetPort {

    private final AttendanceSheetJpaRepository sheetJpaRepository;

    // ========== SaveAttendanceSheetPort ==========

    @Override
    public AttendanceSheet save(AttendanceSheet sheet) {
        return sheetJpaRepository.save(sheet);
    }

    // ========== LoadAttendanceSheetPort ==========

    @Override
    public Optional<AttendanceSheet> findById(Long id) {
        return sheetJpaRepository.findById(id);
    }

    @Override
    public Optional<AttendanceSheet> findByScheduleId(Long scheduleId) {
        return sheetJpaRepository.findByScheduleId(scheduleId);
    }

    @Override
    public List<AttendanceSheet> findByScheduleIds(List<Long> scheduleIds) {
        if (scheduleIds == null || scheduleIds.isEmpty()) {
            return List.of();
        }
        return sheetJpaRepository.findByScheduleIdIn(scheduleIds);
    }

    @Override
    public boolean existsByScheduleId(Long scheduleId) {
        return sheetJpaRepository.existsByScheduleId(scheduleId);
    }

    @Override
    public List<AttendanceSheet> findActiveSheets() {
        return sheetJpaRepository.findByActiveTrue();
    }

    @Override
    public List<AttendanceSheet> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return sheetJpaRepository.findAllById(ids);
    }

    // ========== DeleteAttendanceSheetPort ==========

    @Override
    public void deleteByScheduleId(Long scheduleId) {
        sheetJpaRepository.deleteByScheduleId(scheduleId);
    }

    @Override
    public void delete(AttendanceSheet sheet) {
        sheetJpaRepository.delete(sheet);
    }
}
