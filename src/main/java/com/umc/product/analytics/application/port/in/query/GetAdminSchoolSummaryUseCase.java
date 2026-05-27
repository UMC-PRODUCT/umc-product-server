package com.umc.product.analytics.application.port.in.query;

import com.umc.product.analytics.application.port.in.query.dto.AdminSchoolSummaryInfo;
import com.umc.product.analytics.application.port.in.query.dto.AdminSchoolSummaryQuery;
import org.springframework.data.domain.Page;

public interface GetAdminSchoolSummaryUseCase {

    Page<AdminSchoolSummaryInfo> getSchoolSummaries(AdminSchoolSummaryQuery query);
}
