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

    @Override
    public void delete(AttendanceRecord record) {
        recordJpaRepository.delete(record);
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
    public List<AttendanceRecord> findByChallengerId(Long challengerId) {
        return recordJpaRepository.findByChallengerId(challengerId);
    }

    @Override
    public Optional<AttendanceRecord> findBySheetIdAndChallengerId(Long sheetId, Long challengerId) {
        return recordJpaRepository.findBySheetIdAndChallengerId(sheetId, challengerId);
    }

    @Override
    public List<AttendanceRecord> findPendingRecordsBySheetId(Long sheetId) {
        return recordJpaRepository.findPendingRecordsBySheetId(sheetId);
    }

    @Override
    public boolean existsBySheetIdAndChallengerId(Long sheetId, Long challengerId) {
        return recordJpaRepository.existsBySheetIdAndChallengerId(sheetId, challengerId);
    }

    // ========== DeleteAttendanceRecordPort ==========

    @Override
    public void deleteAllBySheetId(Long sheetId) {
        recordJpaRepository.deleteAllByAttendanceSheetId(sheetId);
    }
}
