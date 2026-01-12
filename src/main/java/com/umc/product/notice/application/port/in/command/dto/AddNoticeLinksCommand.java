package com.umc.product.notice.application.port.in.command.dto;

import java.util.List;

public record AddNoticeLinksCommand(
        Long noticeId,
        Long requesterId,
        List<String> links
) {
}
