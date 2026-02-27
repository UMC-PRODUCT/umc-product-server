package com.umc.product.notice.application.port.out;

import com.umc.product.notice.domain.Notice;

public interface SaveNoticePort {
    Notice save(Notice notice);

    void delete(Notice notice);

}
