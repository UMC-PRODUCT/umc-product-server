package com.umc.product.schedule.application.service;

import com.umc.product.schedule.application.port.in.CreateAttendanceSheetUseCase;
import com.umc.product.schedule.application.port.in.UpdateAttendanceSheetUseCase;
import com.umc.product.schedule.application.port.in.command.CreateAttendanceSheetCommand;
import com.umc.product.schedule.application.port.in.command.UpdateAttendanceSheetCommand;
import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceSheetCommandService implements CreateAttendanceSheetUseCase, UpdateAttendanceSheetUseCase {

    @Override
    public AttendanceSheetId create(CreateAttendanceSheetCommand command) {
        // TODO: 구현 필요
        return new AttendanceSheetId(1L);
    }

    @Override
    public void update(UpdateAttendanceSheetCommand command) {
        // TODO: 구현 필요
    }

    @Override
    public void deactivate(AttendanceSheetId sheetId) {
        // TODO: 구현 필요
    }

    @Override
    public void activate(AttendanceSheetId sheetId) {
        // TODO: 구현 필요
    }
}
