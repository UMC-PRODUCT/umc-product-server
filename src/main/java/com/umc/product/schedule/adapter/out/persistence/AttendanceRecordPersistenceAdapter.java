package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.application.port.out.DeleteAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.SaveAttendanceRecordPort;
import com.umc.product.schedule.domain.AttendanceRecord;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceRecordPersistenceAdapter implements SaveAttendanceRecordPort, LoadAttendanceRecordPort,
        DeleteAttendanceRecordPort {

    private final AttendanceRecordJpaRepository recordJpaRepository;

    // ========== SaveAttendanceRecordPort ==========

    @Override
    public AttendanceRecord save(AttendanceRecord record) {
        return recordJpaRepository.save(record);
    }

    @Override
    public void saveAllRecords(List<AttendanceRecord> records) {
        recordJpaRepository.saveAll(records);
    }

    // ========== LoadAttendanceRecordPort ==========

    @Override
    public Optional<AttendanceRecord> findById(Long id) {
        return recordJpaRepository.findById(id);
    }

    @Override
    public List<AttendanceRecord> findByAttendanceSheetId(Long sheetId) {
        return recordJpaRepository.findByAttendanceSheetId(sheetId);
    }

    @Override
    public List<AttendanceRecord> findByMemberId(Long memberId) {
        return recordJpaRepository.findByMemberId(memberId);
    }

    @Override
    public Optional<AttendanceRecord> findBySheetIdAndMemberId(Long sheetId, Long memberId) {
        return recordJpaRepository.findByAttendanceSheetIdAndMemberId(sheetId, memberId);
    }

    @Override
    public List<AttendanceRecord> findPendingRecordsBySheetId(Long sheetId) {
        return null;
    }

    @Override
    public boolean existsBySheetIdAndMemberId(Long sheetId, Long memberId) {
        return recordJpaRepository.existsByAttendanceSheetIdAndMemberId(sheetId, memberId);
    }

    // ========== DeleteAttendanceRecordPort ==========

    @Override
    public void deleteAllBySheetId(Long sheetId) {
        recordJpaRepository.deleteAllByAttendanceSheetId(sheetId);
    }

    @Override
    public void delete(AttendanceRecord record) {
        recordJpaRepository.delete(record);
    }
}
