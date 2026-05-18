package com.umc.product.notice.application.port.in.command.dto;

import lombok.Builder;

import java.util.List;

/**
 * 공지사항 투표 응답 수정/취소 Command.
 * {@code selectedOptionIds}가 빈 리스트면 응답 취소로 동작하며, {@code null}인 경우 예외가 발생한다.
 */
@Builder
public record UpdateNoticeVoteResponseCommand(
    Long noticeId,
    Long respondentMemberId,
    List<Long> selectedOptionIds
) {
}
