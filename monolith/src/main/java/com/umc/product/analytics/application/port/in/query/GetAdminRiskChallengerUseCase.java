package com.umc.product.analytics.application.port.in.query;

import org.springframework.data.domain.Page;

import com.umc.product.analytics.application.port.in.query.dto.AdminRiskChallengerInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminRiskChallengerQuery;

public interface GetAdminRiskChallengerUseCase {

    Page<AdminRiskChallengerInfo> getRiskChallengers(AdminRiskChallengerQuery query);
}
