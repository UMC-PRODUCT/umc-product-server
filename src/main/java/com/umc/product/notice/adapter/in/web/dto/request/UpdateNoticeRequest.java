package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.UpdateNoticeCommand;
import jakarta.validation.constraints.NotBlank;

public record UpdateNoticeRequest(

    @NotBlank(message = "공지 제목은 비어 있을 수 없습니다.")
    String title,
    @NotBlank(message = "공지 내용은 비어 있을 수 없습니다.")
    String content

) {

    public UpdateNoticeCommand toCommand(Long memberId, Long noticeId) {
        return new UpdateNoticeCommand(
            memberId,
            noticeId,
            title,
            content
        );
    }
}
