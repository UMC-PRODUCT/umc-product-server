package com.umc.product.schedule.domain;

import java.time.ZoneId;

@Deprecated(since = "v1.5.0", forRemoval = true)
public final class ScheduleConstants {

    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private ScheduleConstants() {
    }
}
