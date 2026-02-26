package com.umc.product.notice.application.port.out;

import com.umc.product.notice.domain.NoticeLink;
import java.util.List;

public interface SaveNoticeLinkPort {
    NoticeLink saveLink(NoticeLink noticeLink);

    List<NoticeLink> saveAllLinks(List<NoticeLink> noticeLinks);

    void deleteLink(NoticeLink noticeLink);

    void deleteAllLinksByNoticeId(Long noticeId);

}
