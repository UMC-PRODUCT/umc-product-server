package com.umc.product.notice.application.port.out;

import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.dto.NoticeClassification;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadNoticePort {
    Optional<Notice> findNoticeById(Long id);

    Page<Notice> findNoticesByClassification(NoticeClassification classification, Pageable pageable);

    Page<Notice> findNoticesByKeyword(String keyword, NoticeClassification classification, Pageable pageable);

    Page<Notice> findAllNotices(Pageable pageable);

}
