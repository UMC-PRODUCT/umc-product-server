package com.umc.product.analytics.application.port.out;

import com.umc.product.analytics.application.port.in.query.dto.AdminRiskChallengerInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminRiskChallengerQuery;
import com.umc.product.analytics.domain.AdminAnalyticsScope;
import org.springframework.data.domain.Page;

public interface LoadAdminRiskChallengerAnalyticsPort {

    Page<AdminRiskChallengerInfo> getRiskChallengers(AdminAnalyticsScope scope, AdminRiskChallengerQuery query);
}
