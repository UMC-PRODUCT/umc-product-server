package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.application.port.out.SaveAttendanceRecordPort;
import com.umc.product.schedule.domain.AttendanceRecord;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceRecordPersistenceAdapter implements SaveAttendanceRecordPort {

    private AttendanceRecordJpaRepository recordJpaRepository;

    @Override
    public AttendanceRecord save(AttendanceRecord record) {
        return null;
    }

    @Override
    public void saveAllRecords(List<AttendanceRecord> records) {
        records.stream().map(record -> recordJpaRepository.save(record));
    }

    @Override
    public void delete(AttendanceRecord record) {

    }
}
