package com.umc.product.analytics.application.port.in.query;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsOverviewInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsOverviewQuery;

public interface GetAdminOperationsOverviewUseCase {

    AdminOperationsOverviewInfo getOperationsOverview(AdminOperationsOverviewQuery query);
}
