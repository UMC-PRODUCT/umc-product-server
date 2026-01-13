package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.ApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationListQuery;

public interface GetApplicationListUseCase {
    ApplicationListInfo get(GetApplicationListQuery query);
}
