package com.umc.product.notice.application.port.out;

import com.umc.product.notice.domain.NoticeVote;
import java.util.List;

public interface SaveNoticeVotePort {
    NoticeVote saveVote(NoticeVote noticeVote);

    void deleteVote(NoticeVote noticeVote);

    void deleteAllVotesByNoticeId(Long noticeId);
}
