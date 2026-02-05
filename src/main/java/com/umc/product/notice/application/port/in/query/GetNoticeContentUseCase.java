package com.umc.product.notice.application.port.in.query;

import com.umc.product.notice.application.port.in.query.dto.NoticeImageInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeLinkInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeVoteInfo;
import java.util.List;

public interface GetNoticeContentUseCase {
    List<NoticeLinkInfo> findLinkByNoticeId(Long noticeId);

    List<NoticeVoteInfo> findVoteByNoticeId(Long noticeId);

    List<NoticeImageInfo> findImageByNoticeId(Long noticeId);
}
