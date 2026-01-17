package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.AddNoticeLinksCommand;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AddNoticeLinksRequest(

        @NotEmpty(message = "링크 리스트는 비어 있을 수 없습니다.")
        List<String> links
) {

    public AddNoticeLinksCommand toCommand() {
        return new AddNoticeLinksCommand(links);
    }
}
