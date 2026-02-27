package com.umc.product.notice.application.port.out;

import com.umc.product.notice.domain.NoticeLink;
import java.util.List;
import java.util.Optional;

public interface LoadNoticeLinkPort {
    Optional<NoticeLink> findLinkById(Long id);

    List<NoticeLink> findLinksByNoticeId(Long noticeId);

    boolean existsLinkByNoticeId(Long noticeId);

    int findNextLinkDisplayOrder(Long noticeId);

    int countLinkByNoticeId(Long noticeId);

}
