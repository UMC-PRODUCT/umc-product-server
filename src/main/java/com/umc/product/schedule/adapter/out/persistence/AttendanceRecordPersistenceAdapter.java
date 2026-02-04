package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.application.port.in.query.dto.PendingAttendanceInfo;
import com.umc.product.schedule.application.port.out.DeleteAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.LoadAttendanceRecordPort;
import com.umc.product.schedule.application.port.out.SaveAttendanceRecordPort;
import com.umc.product.schedule.domain.AttendanceRecord;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceRecordPersistenceAdapter implements SaveAttendanceRecordPort, LoadAttendanceRecordPort,
    DeleteAttendanceRecordPort {

    private static final List<AttendanceStatus> APPROVAL_PENDING_STATUSES = List.of(
        AttendanceStatus.PRESENT_PENDING,
        AttendanceStatus.LATE_PENDING,
        AttendanceStatus.EXCUSED_PENDING
    );

    private final AttendanceRecordJpaRepository recordJpaRepository;
    private final AttendanceRecordQueryRepository recordQueryRepository;

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
        return recordJpaRepository.findByAttendanceSheetIdAndStatusIn(sheetId, APPROVAL_PENDING_STATUSES);
    }

    @Override
    public boolean existsBySheetIdAndMemberId(Long sheetId, Long memberId) {
        return recordJpaRepository.existsByAttendanceSheetIdAndMemberId(sheetId, memberId);
    }

    @Override
    public List<AttendanceRecord> findByAttendanceSheetIds(List<Long> sheetIds) {
        if (sheetIds == null || sheetIds.isEmpty()) {
            return List.of();
        }
        return recordJpaRepository.findByAttendanceSheetIdIn(sheetIds);
    }

    @Override
    public List<PendingAttendanceInfo> findPendingWithMemberInfo(Long sheetId) {
        return recordQueryRepository.findPendingWithMemberInfo(sheetId);
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
