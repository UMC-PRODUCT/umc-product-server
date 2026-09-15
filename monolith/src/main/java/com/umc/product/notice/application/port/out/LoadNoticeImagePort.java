package com.umc.product.notice.application.port.out;

import java.util.List;
import java.util.Optional;

import com.umc.product.notice.domain.NoticeImage;

public interface LoadNoticeImagePort {
    Optional<NoticeImage> findImageById(Long id);

    List<NoticeImage> findImagesByNoticeId(Long noticeId);

    boolean existsImageByNoticeId(Long noticeId);

    int findNextImageDisplayOrder(Long noticeId);

    int countImageByNoticeId(Long noticeId);
}
