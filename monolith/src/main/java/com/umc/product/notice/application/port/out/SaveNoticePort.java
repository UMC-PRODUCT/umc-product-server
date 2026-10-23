package com.umc.product.notice.application.port.out;

import java.time.Instant;

import com.umc.product.notice.domain.Notice;

public interface SaveNoticePort {
    Notice save(Notice notice);

    void delete(Notice notice);

    void incrementViewCount(Long noticeId);

    void updateUpdatedAt(Long noticeId, Instant updatedAt);
}
