package com.umc.product.analytics.application.port.in.query;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsStudyGroupsInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsStudyGroupsQuery;

public interface GetAdminOperationsStudyGroupsUseCase {

    AdminOperationsStudyGroupsInfo getOperationsStudyGroups(AdminOperationsStudyGroupsQuery query);
}
