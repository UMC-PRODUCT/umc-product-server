package com.umc.product.analytics.application.port.in.query;

import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsAttendanceInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminOperationsAttendanceQuery;

public interface GetAdminOperationsAttendanceUseCase {

    AdminOperationsAttendanceInfo getOperationsAttendance(AdminOperationsAttendanceQuery query);
}
