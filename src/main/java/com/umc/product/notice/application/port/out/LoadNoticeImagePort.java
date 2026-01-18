package com.umc.product.notice.application.port.out;

import com.umc.product.notice.domain.NoticeImage;
import java.util.List;
import java.util.Optional;

public interface LoadNoticeImagePort {
    Optional<NoticeImage> findImageById(Long id);
    Optional<List<NoticeImage>> findImagesByNoticeId(Long noticeId);
    boolean existsImageByNoticeId(Long noticeId);
}
