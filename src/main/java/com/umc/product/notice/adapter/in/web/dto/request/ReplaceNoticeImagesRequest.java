package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.ReplaceNoticeImagesCommand;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReplaceNoticeImagesRequest(
    @NotNull(message = "공지 이미지 목록은 비어 있을 수 없습니다.")
    List<String> imageIds
) {
    public ReplaceNoticeImagesCommand toCommand() {
        return new ReplaceNoticeImagesCommand(imageIds);
    }
}
