package com.umc.product.notice.application.port.out;

import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeRead;
import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface LoadNoticePort {
    Optional<Notice> findNoticeById(Long id);

    List<Notice> findAllNotices(Pageable pageable);

}
