package com.umc.product.notice.application.port.in.query;

import com.umc.product.notice.domain.enums.NoticeReadStatus;
import com.umc.product.notice.domain.enums.NoticeReadStatusFilterType;

public record GetNoticeStatusCommand(
        Long noticeId,
        Long requesterId,
        NoticeReadStatusFilterType filterType,
        Long organizationId,
        NoticeReadStatus status
) {
}
