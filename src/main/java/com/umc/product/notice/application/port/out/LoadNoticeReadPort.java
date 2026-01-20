package com.umc.product.notice.application.port.out;

import com.umc.product.notice.domain.NoticeRead;
import java.util.List;

public interface LoadNoticeReadPort {

    List<NoticeRead> findNoticeReadByNoticeId(Long noticeId);

    List<Long> findUnreadChallengerIdByNoticeId(Long noticeId);

    boolean existsRead(Long noticeId, Long challengerId);

    long countReadsByNoticeId(Long noticeId);

}
