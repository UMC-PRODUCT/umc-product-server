package com.umc.product.schedule.application.service;

import com.umc.product.schedule.application.port.in.ApproveAttendanceUseCase;
import com.umc.product.schedule.application.port.in.CheckAttendanceUseCase;
import com.umc.product.schedule.application.port.in.command.CheckAttendanceCommand;
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
        return new AttendanceRecordId(1L);
    }

    @Override
    public void approve(AttendanceRecordId recordId, Long confirmerId) {
        // TODO: 구현 필요
    }

    @Override
    public void reject(AttendanceRecordId recordId, Long confirmerId) {
        // TODO: 구현 필요
    }
}
