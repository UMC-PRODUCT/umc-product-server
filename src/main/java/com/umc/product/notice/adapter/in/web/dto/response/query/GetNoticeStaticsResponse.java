package com.umc.product.notice.adapter.in.web.dto.response.query;

import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusSummary;

public record GetNoticeStaticsResponse(
    Integer totalCount,
    Integer readCount,
    Integer unreadCount,
    Float readRate // TODO: FE 단에서 직접 계산하도록 해도 좋을 것 같음
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
