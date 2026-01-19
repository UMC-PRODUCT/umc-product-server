package com.umc.product.schedule.application.service;

import com.umc.product.schedule.application.port.in.command.ApproveAttendanceUseCase;
import com.umc.product.schedule.application.port.in.command.CheckAttendanceCommand;
import com.umc.product.schedule.application.port.in.command.CheckAttendanceUseCase;
import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceCommandService implements CheckAttendanceUseCase, ApproveAttendanceUseCase {

    @Override
    public AttendanceRecordId check(CheckAttendanceCommand command) {
        // TODO: 구현 필요
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void approve(AttendanceRecordId recordId, Long confirmerId) {
        // TODO: 구현 필요
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void reject(AttendanceRecordId recordId, Long confirmerId) {
        // TODO: 구현 필요
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
