package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.command.dto.UpdateNoticeCommand;
import com.umc.product.notice.dto.NoticeTargetInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateNoticeRequest(

        @NotBlank(message = "공지 제목은 비어 있을 수 없습니다.")
        String title,
        @NotBlank(message = "공지 내용은 비어 있을 수 없습니다.")
        String content,
        @NotNull(message = "공지 대상 정보는 비어 있을 수 없습니다.")
        NoticeTargetInfo targetInfo,
        @NotNull(message = "알림 발송 여부는 비어 있을 수 없습니다.")
        Boolean shouldNotify, /* 알림 발송 여부 */
        List<Long> imageIds,
        List<String> links,
        List<Long> voteIds
) {

    public UpdateNoticeCommand toCommand(Long authorChallengerId, Long noticeId) {
        return new UpdateNoticeCommand(
                authorChallengerId,
                noticeId,
                title,
                content,
                targetInfo,
                shouldNotify,
                imageIds,
                links,
                voteIds
        );
    }
}
