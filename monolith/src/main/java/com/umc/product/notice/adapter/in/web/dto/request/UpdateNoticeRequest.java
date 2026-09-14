package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.UpdateNoticeCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "공지사항 수정 요청. 제목, 내용, 필독 여부를 수정할 수 있으며, 대상 범위는 변경할 수 없습니다.")
public record UpdateNoticeRequest(

    @NotBlank(message = "공지 제목은 비어 있을 수 없습니다.")
    String title,

    @NotBlank(message = "공지 내용은 비어 있을 수 없습니다.")
    String content,

    @Schema(description = "UPMS 필독 공지시 사용. true로 설정하면 공지 목록 최상단에 고정됩니다.", defaultValue = "false")
    boolean mustRead

) {

    public UpdateNoticeCommand toCommand(Long memberId, Long noticeId) {
        return new UpdateNoticeCommand(
            memberId,
            noticeId,
            title,
            content,
            mustRead
        );
    }
}
