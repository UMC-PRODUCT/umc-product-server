package com.umc.product.analytics.application.port.out;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsStudyGroupsInfo;
import com.umc.product.analytics.domain.AdminAnalyticsScope;
import java.time.Instant;

public interface LoadAdminOperationsStudyGroupsPort {

    AdminOperationsStudyGroupsInfo getOperationsStudyGroups(AdminAnalyticsScope scope, Instant from, Instant to);
}
