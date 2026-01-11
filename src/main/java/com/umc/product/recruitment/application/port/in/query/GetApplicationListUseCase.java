package com.umc.product.recruitment.application.port.in.query;

public interface GetApplicationListUseCase {
    ApplicationListInfo get(GetApplicationListQuery query);
}
