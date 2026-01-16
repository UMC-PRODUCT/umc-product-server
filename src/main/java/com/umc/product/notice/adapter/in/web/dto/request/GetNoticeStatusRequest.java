package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.query.dto.GetNoticeStatusQuery;
import com.umc.product.notice.domain.enums.NoticeReadStatus;
import com.umc.product.notice.domain.enums.NoticeReadStatusFilterType;

public record GetNoticeStatusRequest(
        Long cursorId, /* 공지를 읽은 경우: NoticeReadId, 공지를 읽지 않은 경우: ChallengerId */
        NoticeReadStatusFilterType filterType,
        Long organizationId, /* 필터 사용시 지부/학교 id */
        NoticeReadStatus status
) {
    public GetNoticeStatusQuery toQuery(Long noticeId) {
        return new GetNoticeStatusQuery(cursorId, noticeId, filterType, organizationId, status);
    }
}
