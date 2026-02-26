package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.CreateNoticeCommand;
import com.umc.product.notice.dto.NoticeTargetInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "공지사항 생성 요청")
public record CreateNoticeRequest(
    @Schema(description = "공지사항 제목")
    @NotBlank(message = "제목은 필수입니다.")
    String title,

    @Schema(description = "공지사항 본문 내용")
    @NotBlank(message = "내용은 필수입니다.")
    String content,

    @Schema(description = "공지 생성 시 대상자에게 푸시 알림을 보낼지 여부. true면 즉시 알림 발송")
    @NotNull(message = "알림 발송 여부는 필수입니다.")
    Boolean shouldNotify,

    @Schema(description = "공지 대상 범위 설정. 어떤 기수/지부/학교/파트에 공지를 보낼지 지정")
    @Valid
    @NotNull(message = "대상 정보는 필수입니다")
    NoticeTargetInfo targetInfo
) {
    public CreateNoticeCommand toCommand(Long memberId) {
        return new CreateNoticeCommand(memberId, title, content, shouldNotify, targetInfo);
    }
}
