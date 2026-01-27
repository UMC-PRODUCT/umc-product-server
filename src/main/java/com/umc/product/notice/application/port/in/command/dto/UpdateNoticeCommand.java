package com.umc.product.notice.application.port.in.command.dto;

import com.umc.product.notice.dto.NoticeTargetInfo;
import java.util.List;

public record UpdateNoticeCommand(
        Long memberId,
        Long noticeId,
        String title,
        String content,
        NoticeTargetInfo targetInfo,
        Boolean shouldNotify, /* 알림 발송 여부 */
        List<String> imageIds,
        List<String> links,
        List<Long> voteIds
) {
}
