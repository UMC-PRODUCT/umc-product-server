package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.ApplicationListForAdminInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationListForAdminQuery;

public interface GetApplicationListForAdminUseCase {
    ApplicationListForAdminInfo get(GetApplicationListForAdminQuery query);
}
