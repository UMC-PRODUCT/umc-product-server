package com.umc.product.analytics.application.port.in.query;

import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardQuery;
import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardSummaryInfo;

public interface GetAdminDashboardSummaryUseCase {

    AdminDashboardSummaryInfo getSummary(AdminDashboardQuery query);
}
