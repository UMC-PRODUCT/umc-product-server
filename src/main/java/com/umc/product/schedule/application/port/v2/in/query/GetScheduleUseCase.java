package com.umc.product.schedule.application.port.v2.in.query;

import com.umc.product.schedule.application.port.v2.in.query.dto.ScheduleInfo;
import java.time.Instant;
import java.util.List;

public interface GetScheduleUseCase {

    List<ScheduleInfo> getMySchedule(
        Instant from,
        Instant to,
        Boolean isAttendanceRequired,
        Long memberId
    );
}
