package com.umc.product.recruitment.application.port.in.query;

public interface GetMyApplicationListUseCase {
    MyApplicationListInfo get(GetMyApplicationListQuery query);
}
