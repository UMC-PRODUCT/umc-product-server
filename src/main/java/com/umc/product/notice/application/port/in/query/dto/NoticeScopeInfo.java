package com.umc.product.notice.application.port.in.query.dto;

import com.umc.product.notice.domain.enums.NoticeClassification;

/*
 * 공지 전체조회 페이지에 있는 필터 조회
 */
public record NoticeScopeInfo(
    NoticeClassification type,
    String displayName
) {
}
