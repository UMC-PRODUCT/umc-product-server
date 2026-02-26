package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.ScheduleDetailInfo;

public interface GetScheduleDetailUseCase {

    ScheduleDetailInfo getScheduleDetail(Long scheduleId);
}
