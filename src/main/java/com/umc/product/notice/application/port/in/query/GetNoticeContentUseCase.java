package com.umc.product.notice.application.port.in.query;

import com.umc.product.notice.application.port.in.query.dto.NoticeImageInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeLinkInfo;
import com.umc.product.survey.application.port.in.query.dto.VoteInfo;
import java.util.List;

public interface GetNoticeContentUseCase {
    List<NoticeLinkInfo> findLinkByNoticeId(Long noticeId);

    VoteInfo findVoteByNoticeId(Long noticeId, Long memberId);

    List<NoticeImageInfo> findImageByNoticeId(Long noticeId);
}
