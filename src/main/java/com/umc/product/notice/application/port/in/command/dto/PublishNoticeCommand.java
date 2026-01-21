package com.umc.product.notice.application.port.in.command.dto;

/*
 * 작성완료 눌렀을 때 DRAFT -> PUBLISHED
 * */
public record PublishNoticeCommand(
        Long memberId,
        Long noticeId
) {
}
