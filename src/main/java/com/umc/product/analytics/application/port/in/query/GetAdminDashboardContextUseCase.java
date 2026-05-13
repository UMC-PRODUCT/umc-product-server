package com.umc.product.analytics.application.port.in.query;

import com.umc.product.analytics.application.port.in.query.dto.AdminDashboardContextInfo;

public interface GetAdminDashboardContextUseCase {

    AdminDashboardContextInfo getContext(Long requesterMemberId);
}
