package com.umc.product.notice.application.port.in.query.dto;

import java.util.List;

public record NoticeReadStatusResult(
        List<NoticeReadStatusInfo> content,
        Long cursorId, /* 공지를 읽은 경우: NoticeReadId, 공지를 읽지 않은 경우: ChallengerId */
        boolean hasNext
) {
}
