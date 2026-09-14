package com.umc.product.notice.application.port.out;

import java.util.List;

import com.umc.product.notice.domain.NoticeLink;

public interface SaveNoticeLinkPort {
    NoticeLink saveLink(NoticeLink noticeLink);

    List<NoticeLink> saveAllLinks(List<NoticeLink> noticeLinks);

    void deleteLink(NoticeLink noticeLink);

    void deleteAllLinksByNoticeId(Long noticeId);

}
