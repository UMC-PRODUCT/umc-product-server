package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.ReplaceNoticeLinksCommand;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReplaceNoticeLinksRequest(
    @NotNull(message = "공지 링크 목록은 비어 있을 수 없습니다.")
    List<String> links
) {
    public ReplaceNoticeLinksCommand toCommand() {
        return new ReplaceNoticeLinksCommand(links);
    }
}
