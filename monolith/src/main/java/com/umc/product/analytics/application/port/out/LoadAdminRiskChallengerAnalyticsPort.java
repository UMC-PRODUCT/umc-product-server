package com.umc.product.analytics.application.port.out;

import org.springframework.data.domain.Page;

import com.umc.product.analytics.application.port.in.query.dto.AdminRiskChallengerInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminRiskChallengerQuery;
import com.umc.product.analytics.domain.AdminAnalyticsScope;

public interface LoadAdminRiskChallengerAnalyticsPort {

    Page<AdminRiskChallengerInfo> getRiskChallengers(AdminAnalyticsScope scope, AdminRiskChallengerQuery query);
}
