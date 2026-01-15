package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.query.dto.SchoolSearchCondition;

public record SchoolListRequest(
        String keyword,
        Long chapterId
) {

    public SchoolSearchCondition toCondition() {
        return new SchoolSearchCondition(
                keyword,
                chapterId
        );
    }
}

