package com.umc.product.notice.application.port.out;

import java.util.List;
import java.util.Optional;

import com.umc.product.notice.domain.NoticeLink;

public interface LoadNoticeLinkPort {
    Optional<NoticeLink> findLinkById(Long id);

    List<NoticeLink> findLinksByNoticeId(Long noticeId);

    boolean existsLinkByNoticeId(Long noticeId);

    List<Long> listNoticeIdsWithLinks(List<Long> noticeIds);

    int findNextLinkDisplayOrder(Long noticeId);

    int countLinkByNoticeId(Long noticeId);

}
