package com.umc.product.notice.application.port.in.command.dto;

import com.umc.product.notice.dto.NoticeTargetInfo;

public record CreateNoticeCommand(
    Long memberId, /* 공지 작성자 멤버 ID */
    String title,
    String content,
    Boolean shouldNotify, /* 알림 발송 여부 */
    NoticeTargetInfo targetInfo
) {
}
