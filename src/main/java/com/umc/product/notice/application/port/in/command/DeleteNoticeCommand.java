package com.umc.product.notice.application.port.in.command;

public record DeleteNoticeCommand(
        Long noticeId,
        Long deleteChallengerId // 권한 확인용
) {
}
