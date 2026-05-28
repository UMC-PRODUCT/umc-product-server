package com.umc.product.analytics.application.port.out;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsPointsInfo;
import com.umc.product.analytics.domain.AdminAnalyticsScope;
import java.time.Instant;

public interface LoadAdminOperationsPointsPort {

    AdminOperationsPointsInfo getOperationsPoints(AdminAnalyticsScope scope, Instant from, Instant to);
}
