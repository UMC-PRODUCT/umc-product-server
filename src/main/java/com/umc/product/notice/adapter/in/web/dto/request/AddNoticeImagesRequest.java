package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.AddNoticeImagesCommand;
import java.util.List;

public record AddNoticeImagesRequest(
        Long noticeId,
        List<Long> imageIds
) {

    public AddNoticeImagesCommand toCommand() {
        return new AddNoticeImagesCommand(noticeId, imageIds);
    }
}
