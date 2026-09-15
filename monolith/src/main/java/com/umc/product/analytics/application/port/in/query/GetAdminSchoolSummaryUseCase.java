package com.umc.product.analytics.application.port.in.query;

import org.springframework.data.domain.Page;

import com.umc.product.analytics.application.port.in.query.dto.AdminSchoolSummaryInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminSchoolSummaryQuery;

public interface GetAdminSchoolSummaryUseCase {

    Page<AdminSchoolSummaryInfo> getSchoolSummaries(AdminSchoolSummaryQuery query);
}
