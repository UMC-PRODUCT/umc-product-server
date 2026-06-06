package com.umc.product.analytics.application.port.out;

import java.time.Instant;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsStudyGroupsInfo;
import com.umc.product.analytics.domain.AdminAnalyticsScope;

public interface LoadAdminOperationsStudyGroupsPort {

    AdminOperationsStudyGroupsInfo getOperationsStudyGroups(AdminAnalyticsScope scope, Instant from, Instant to);
}
