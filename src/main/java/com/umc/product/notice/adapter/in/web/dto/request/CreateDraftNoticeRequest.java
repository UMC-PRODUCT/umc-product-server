package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.CreateNoticeCommand;
import com.umc.product.notice.dto.NoticeTargetInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDraftNoticeRequest(
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String content,

        @NotNull(message = "알림 발송 여부는 필수입니다.")
        Boolean shouldNotify, /* 알림 발송 여부 */

        @Valid
        @NotNull(message = "대상 정보는 필수입니다")
        NoticeTargetInfo targetInfo
) {

    public CreateNoticeCommand toCommand() {
        return new CreateNoticeCommand(title, content, shouldNotify, targetInfo);
    }

}
