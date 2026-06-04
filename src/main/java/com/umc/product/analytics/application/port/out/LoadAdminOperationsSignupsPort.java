package com.umc.product.analytics.application.port.out;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsSignupsInfo;
import com.umc.product.analytics.domain.AdminAnalyticsScope;
import java.time.Instant;

public interface LoadAdminOperationsSignupsPort {

    AdminOperationsSignupsInfo getOperationsSignups(AdminAnalyticsScope scope, Instant from, Instant to);
}
