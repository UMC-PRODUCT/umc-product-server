package com.umc.product.notice.application.port.in.query.dto;

import com.umc.product.notice.domain.enums.NoticeReadStatus;
import com.umc.product.notice.domain.enums.NoticeReadStatusFilterType;
import java.util.List;

public record GetNoticeStatusQuery(
        Long cursorId, /* 공지를 읽은 경우: NoticeReadId, 공지를 읽지 않은 경우: ChallengerId */
        Long noticeId,
        NoticeReadStatusFilterType filterType,
        List<Long> organizationId, /* 필터 사용시 지부/학교 id */
        NoticeReadStatus status
) {
}
