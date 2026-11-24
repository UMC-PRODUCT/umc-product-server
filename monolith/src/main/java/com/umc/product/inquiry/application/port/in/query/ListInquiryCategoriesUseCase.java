package com.umc.product.inquiry.application.port.in.query;

import java.util.List;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryCategoryInfo;

public interface ListInquiryCategoriesUseCase {

    /** 활성 카테고리를 표시 순서대로 돌려준다. */
    List<InquiryCategoryInfo> listActive();
}
