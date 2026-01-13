package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.GetMyApplicationListQuery;
import com.umc.product.recruitment.application.port.in.query.dto.MyApplicationListInfo;

public interface GetMyApplicationListUseCase {
    MyApplicationListInfo get(GetMyApplicationListQuery query);
}
