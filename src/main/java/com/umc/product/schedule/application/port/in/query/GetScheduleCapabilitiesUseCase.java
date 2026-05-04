package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.ScheduleCapabilitiesInfo;

public interface GetScheduleCapabilitiesUseCase {

    ScheduleCapabilitiesInfo getCapabilities(Long memberId);
}
