package com.umc.product.notice.application.port.in.query;

public record NoticeReadStatusSummary(
        Integer totalCount,
        Integer readCount,
        Integer unreadCount,
        Float readRate
) {
}
