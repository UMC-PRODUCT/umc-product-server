package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.query.dto.GetNoticeStatusQuery;
import com.umc.product.notice.domain.enums.NoticeReadStatus;
import com.umc.product.notice.domain.enums.NoticeReadStatusFilterType;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record GetNoticeStatusRequest(
    Long cursorId, /* 공지를 읽은 경우: NoticeReadId, 공지를 읽지 않은 경우: ChallengerId */
    @NotNull(message = "필터 타입은 비어 있을 수 없습니다.")
    NoticeReadStatusFilterType filterType,
    List<Long> organizationIds, /* 필터 사용시 지부/학교 id */
    @NotNull(message = "읽음 상태는 비어 있을 수 없습니다.")
    NoticeReadStatus status
) {
    public GetNoticeStatusQuery toQuery(Long noticeId) {
        return new GetNoticeStatusQuery(cursorId, noticeId, filterType, organizationIds, status);
    }
}
