package com.umc.product.notice.application.port.in.query;

import com.umc.product.challenger.domain.enums.OrganizationType;

/*
* 공지 전체조회 페이지에 있는 필터 조회
* */
public record NoticeScopeInfo(
        OrganizationType type,
        String displayName
) {
}
