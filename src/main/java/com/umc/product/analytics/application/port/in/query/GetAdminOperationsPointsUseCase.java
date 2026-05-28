package com.umc.product.analytics.application.port.in.query;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsPointsInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsPointsQuery;

public interface GetAdminOperationsPointsUseCase {

    AdminOperationsPointsInfo getOperationsPoints(AdminOperationsPointsQuery query);
}
