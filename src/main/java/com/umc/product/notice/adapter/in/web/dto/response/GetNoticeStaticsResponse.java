package com.umc.product.notice.adapter.in.web.dto.response;

import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusSummary;

public record GetNoticeStaticsResponse(
        Integer totalCount,
        Integer readCount,
        Integer unreadCount,
        Float readRate
) {
    public static GetNoticeStaticsResponse from(NoticeReadStatusSummary summary) {
        return new GetNoticeStaticsResponse(
                summary.totalCount(),
                summary.readCount(),
                summary.unreadCount(),
                summary.readRate()
        );
    }
}
