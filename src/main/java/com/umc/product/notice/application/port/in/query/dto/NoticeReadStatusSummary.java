package com.umc.product.notice.application.port.in.query.dto;

public record NoticeReadStatusSummary(
        Integer totalCount,
        Integer readCount,
        Integer unreadCount,
        Float readRate
) {
}
