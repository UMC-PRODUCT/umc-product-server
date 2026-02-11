package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.ReplaceNoticeImagesCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReplaceNoticeImagesRequest(
    @Schema(description = "교체할 이미지 ID 목록. 파일 업로드 API에서 받은 이미지 ID를 전달. "
        + "빈 배열이면 기존 이미지 전체 삭제")
    @NotNull(message = "공지 이미지 목록은 비어 있을 수 없습니다.")
    List<String> imageIds
) {
    public ReplaceNoticeImagesCommand toCommand() {
        return new ReplaceNoticeImagesCommand(imageIds);
    }
}
