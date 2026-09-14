package com.umc.product.analytics.application.port.out;

import com.umc.product.analytics.application.port.in.query.dto.AdminSchoolSummaryInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminSchoolSummaryQuery;
import com.umc.product.analytics.domain.AdminAnalyticsScope;
import org.springframework.data.domain.Page;

public interface LoadAdminSchoolAnalyticsPort {

    Page<AdminSchoolSummaryInfo> getSchoolSummaries(AdminAnalyticsScope scope, AdminSchoolSummaryQuery query);
}
