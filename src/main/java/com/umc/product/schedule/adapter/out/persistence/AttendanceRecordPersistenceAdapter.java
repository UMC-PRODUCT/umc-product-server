package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.application.port.out.DeleteAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.SaveAttendanceRecordPort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;
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
        return null;
    }

    @Override
    public void saveAllRecords(List<AttendanceRecord> records) {
        recordJpaRepository.saveAll(records);
    }

    @Override
    public void delete(AttendanceRecord record) {

    }

    // ========== LoadAttendanceRecordPort ==========

    @Override
    public Optional<AttendanceRecord> findById(AttendanceRecordId id) {
        return Optional.empty();
    }

    @Override
    public List<AttendanceRecord> findByAttendanceSheetId(Long sheetId) {
        return recordJpaRepository.findByAttendanceSheetId(sheetId);
    }

    @Override
    public List<AttendanceRecord> findByChallengerId(Long challengerId) {
        return null;
    }

    @Override
    public Optional<AttendanceRecord> findBySheetIdAndChallengerId(Long sheetId, Long challengerId) {
        return Optional.empty();
    }

    @Override
    public List<AttendanceRecord> findPendingRecordsBySheetId(Long sheetId) {
        return null;
    }

    @Override
    public boolean existsBySheetIdAndChallengerId(Long sheetId, Long challengerId) {
        return false;
    }

    // ========== DeleteAttendanceRecordPort ==========
    
    @Override
    public void deleteAllBySheetId(Long sheetId) {
        recordJpaRepository.deleteAllByAttendanceSheetId(sheetId);
    }
}
