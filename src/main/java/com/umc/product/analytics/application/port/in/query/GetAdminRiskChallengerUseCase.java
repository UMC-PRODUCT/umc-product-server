package com.umc.product.analytics.application.port.in.query;

import com.umc.product.analytics.application.port.in.query.dto.AdminRiskChallengerInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminRiskChallengerQuery;
import org.springframework.data.domain.Page;

public interface GetAdminRiskChallengerUseCase {

    Page<AdminRiskChallengerInfo> getRiskChallengers(AdminRiskChallengerQuery query);
}
