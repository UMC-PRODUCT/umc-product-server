package com.umc.product.analytics.application.port.out;

import org.springframework.data.domain.Page;

import com.umc.product.analytics.application.port.in.query.dto.AdminSchoolSummaryInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminSchoolSummaryQuery;
import com.umc.product.analytics.domain.AdminAnalyticsScope;

public interface LoadAdminSchoolAnalyticsPort {

    Page<AdminSchoolSummaryInfo> getSchoolSummaries(AdminAnalyticsScope scope, AdminSchoolSummaryQuery query);
}
