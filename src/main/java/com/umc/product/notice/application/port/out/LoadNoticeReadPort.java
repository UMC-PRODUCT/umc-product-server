package com.umc.product.notice.application.port.out;

import com.umc.product.notice.domain.NoticeRead;
import java.util.List;
import java.util.Map;

public interface LoadNoticeReadPort {

    List<NoticeRead> findNoticeReadByNoticeId(Long noticeId);

    List<Long> findUnreadChallengerIdByNoticeId(Long noticeId);

    boolean existsRead(Long noticeId, Long challengerId);

    long countReadsByNoticeId(Long noticeId);

    /**
     * 여러 공지사항의 읽음 수를 한 번에 조회
     *
     * @param noticeIds 공지사항 ID 목록
     * @return noticeId → 읽은 사람 수
     */
    Map<Long, Long> countReadsByNoticeIds(List<Long> noticeIds);

}
