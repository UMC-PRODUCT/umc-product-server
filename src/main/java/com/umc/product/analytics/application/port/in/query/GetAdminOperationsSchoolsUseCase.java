package com.umc.product.analytics.application.port.in.query;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsSchoolsInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsSchoolsQuery;

public interface GetAdminOperationsSchoolsUseCase {

    AdminOperationsSchoolsInfo getOperationsSchools(AdminOperationsSchoolsQuery query);
}
