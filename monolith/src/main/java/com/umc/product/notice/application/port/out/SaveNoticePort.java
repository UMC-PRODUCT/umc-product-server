package com.umc.product.notice.application.port.out;

import java.time.Instant;
import java.time.LocalDate;

import com.umc.product.notice.domain.Notice;

public interface SaveNoticePort {
    Notice save(Notice notice);

    void delete(Notice notice);

    void incrementViewCount(Long noticeId);

    void updateUpdatedAt(Long noticeId, Instant updatedAt);

    /** 알림 발송 요청과 같은 트랜잭션에서 해당 한국 날짜의 첫 전체 재알림을 선점한다. */
    boolean tryReserveReminder(Long noticeId, LocalDate reminderDate);
}
