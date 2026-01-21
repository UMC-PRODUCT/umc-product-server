package com.umc.product.notice.application.port.out;

import com.umc.product.notice.domain.NoticeRead;

public interface SaveNoticeReadPort {

    NoticeRead saveRead(NoticeRead noticeRead);
    void deleteRead(NoticeRead noticeRead);
}
