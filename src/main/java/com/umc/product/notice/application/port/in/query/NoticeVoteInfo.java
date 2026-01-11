package com.umc.product.notice.application.port.in.query;

public record NoticeVoteInfo(
        Long noticeVoteId,
        Long voteId,
        Integer displayOrder
) {
}
