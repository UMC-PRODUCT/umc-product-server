package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.MyScheduleInfo;
import java.time.Instant;
import java.util.List;

public interface GetMyScheduleUseCase {

    // 캘린더용
    List<MyScheduleInfo> getMyMonthlySchedules(Long memberId, Instant from, Instant to);
}
