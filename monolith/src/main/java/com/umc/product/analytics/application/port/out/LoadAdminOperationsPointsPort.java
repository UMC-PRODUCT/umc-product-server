package com.umc.product.analytics.application.port.out;

import java.time.Instant;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsPointsInfo;
import com.umc.product.analytics.domain.AdminAnalyticsScope;

public interface LoadAdminOperationsPointsPort {

    AdminOperationsPointsInfo getOperationsPoints(AdminAnalyticsScope scope, Instant from, Instant to);
}
