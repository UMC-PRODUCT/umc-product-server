package com.umc.product.organization.domain;

import java.time.Duration;
import java.time.Instant;

public final class GisuActivityDays {

    private GisuActivityDays() {
    }

    public static long calculate(Instant startAt, Instant endAt, Instant now) {
        if (now.isBefore(startAt)) {
            return 0L;
        }

        Instant effectiveEnd = now.isBefore(endAt) ? now : endAt;
        return Duration.between(startAt, effectiveEnd).toDays();
    }
}
