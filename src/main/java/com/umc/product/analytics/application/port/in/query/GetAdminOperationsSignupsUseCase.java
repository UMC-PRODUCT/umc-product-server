package com.umc.product.analytics.application.port.in.query;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsSignupsInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsSignupsQuery;

public interface GetAdminOperationsSignupsUseCase {

    AdminOperationsSignupsInfo getOperationsSignups(AdminOperationsSignupsQuery query);
}
