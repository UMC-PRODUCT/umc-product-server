package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.AddNoticeLinksCommand;
import java.util.List;

public record AddNoticeLinksRequest(
        Long noticeId,
        List<String> links
) {

    public AddNoticeLinksCommand toCommand() {
        return new AddNoticeLinksCommand(noticeId, links);
    }
}
