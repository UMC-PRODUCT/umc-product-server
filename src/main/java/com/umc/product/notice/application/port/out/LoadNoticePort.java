package com.umc.product.notice.application.port.out;

import com.umc.product.notice.domain.Notice;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadNoticePort {
    Optional<Notice> findNoticeById(Long id);

    Page<Notice> findAllNotices(Pageable pageable);

}
