package com.umc.product.notice.application.port.in.query.dto;

import com.umc.product.notice.domain.enums.NoticeReadStatus;
import com.umc.product.notice.domain.enums.NoticeReadStatusFilterType;

public record GetNoticeStatusQuery(
        Long noticeId,
        NoticeReadStatusFilterType filterType,
        Long organizationId,
        NoticeReadStatus status
) {
}
