package com.umc.product.notice.application.port.in.command;

import java.util.List;

public record AddNoticeLinksCommand(
        Long noticeId,           // 공지 ID
        Long requesterId,
        List<String> links
) {
}
