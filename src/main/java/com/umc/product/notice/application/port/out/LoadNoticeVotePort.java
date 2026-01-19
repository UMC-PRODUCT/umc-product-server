package com.umc.product.notice.application.port.out;

import com.umc.product.notice.domain.NoticeVote;
import java.util.List;
import java.util.Optional;

public interface LoadNoticeVotePort {
    Optional<NoticeVote> findVoteById(Long id);

    List<NoticeVote> findVotesByNoticeId(Long noticeId);

    boolean existsVoteByNoticeId(Long noticeId);

    int findNextVoteDisplayOrder(Long noticeId);

    int countVoteByNoticeId(Long noticeId);
}
