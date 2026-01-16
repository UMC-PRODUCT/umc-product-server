package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.AddNoticeImagesCommand;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AddNoticeImagesRequest(
        @NotNull
        Long noticeId,

        @NotEmpty(message = "이미지 ID 리스트는 비어 있을 수 없습니다.")
        List<Long> imageIds
) {

    public AddNoticeImagesCommand toCommand() {
        return new AddNoticeImagesCommand(noticeId, imageIds);
    }
}
