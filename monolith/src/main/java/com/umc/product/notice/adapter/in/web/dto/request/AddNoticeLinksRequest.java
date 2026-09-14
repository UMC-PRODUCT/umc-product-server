package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.AddNoticeLinksCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AddNoticeLinksRequest(

    @NotEmpty(message = "링크 리스트는 비어 있을 수 없습니다.")
    List<String> links
) {

    public AddNoticeLinksCommand toCommand() {
        return new AddNoticeLinksCommand(links);
    }
}
