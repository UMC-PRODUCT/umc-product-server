package com.umc.product.schedule.adapter.out.persistence;

import com.umc.product.schedule.application.port.out.SaveAttendanceSheetPort;
import com.umc.product.schedule.domain.AttendanceSheet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceSheetPersistenceAdapter implements SaveAttendanceSheetPort {

    private AttendanceSheetJpaRepository sheetJpaRepository;

    @Override
    public AttendanceSheet save(AttendanceSheet sheet) {
        return sheetJpaRepository.save(sheet);
    }

    @Override
    public void delete(AttendanceSheet sheet) {

    }
}
