package com.umc.product.analytics.application.port.in.query;

import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardActionQueueInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardActionQueueQuery;

public interface GetAdminDashboardActionQueueUseCase {

    AdminDashboardActionQueueInfo getActionQueue(AdminDashboardActionQueueQuery query);
}
