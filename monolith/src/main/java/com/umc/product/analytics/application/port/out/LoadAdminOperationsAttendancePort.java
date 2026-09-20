package com.umc.product.analytics.application.port.out;

import java.time.Instant;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsAttendanceInfo;
import com.umc.product.analytics.domain.AdminAnalyticsScope;

public interface LoadAdminOperationsAttendancePort {

    AdminOperationsAttendanceInfo getOperationsAttendance(AdminAnalyticsScope scope, Instant from, Instant to);
}
