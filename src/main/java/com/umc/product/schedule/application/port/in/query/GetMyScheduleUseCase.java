package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.MyScheduleCalendarInfo;
import java.util.List;

public interface GetMyScheduleUseCase {

    // 캘린더용
    List<MyScheduleCalendarInfo> getMyMonthlySchedules(Long memberId, int year, int month);
}
