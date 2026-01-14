package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.ApplicationDetailInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationDetailQuery;

public interface GetApplicationDetailUseCase {
    ApplicationDetailInfo get(GetApplicationDetailQuery query);
}
