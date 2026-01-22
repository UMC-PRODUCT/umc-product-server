package com.umc.product.schedule.application.service.query;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.application.port.in.query.GetScheduleUseCase;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleInfo;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleQueryService implements GetScheduleUseCase {

    private final LoadSchedulePort loadSchedulePort;

    @Override
    public ScheduleInfo getById(Long scheduleId) {
        Schedule schedule = loadSchedulePort.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.SCHEDULE_NOT_FOUND));
        return ScheduleInfo.from(schedule);
    }

    @Override
    public boolean existsById(Long scheduleId) {
        return loadSchedulePort.existsById(scheduleId);
    }
}
