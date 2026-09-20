package com.umc.product.analytics.application.port.out;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsSchoolsInfo;
import com.umc.product.analytics.domain.AdminAnalyticsScope;

public interface LoadAdminOperationsSchoolsPort {

    AdminOperationsSchoolsInfo getOperationsSchools(AdminAnalyticsScope scope);
}
