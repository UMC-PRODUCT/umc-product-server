package com.umc.product.notice.application.port.out;

import java.util.List;
import java.util.Optional;

import com.umc.product.notice.domain.NoticeVote;

public interface LoadNoticeVotePort {
    Optional<NoticeVote> findVoteById(Long id);

    Optional<NoticeVote> findVoteByNoticeId(Long noticeId);

    boolean existsVoteByNoticeId(Long noticeId);

    List<Long> listNoticeIdsWithVotes(List<Long> noticeIds);
}
