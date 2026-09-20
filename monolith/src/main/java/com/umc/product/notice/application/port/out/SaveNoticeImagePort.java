package com.umc.product.notice.application.port.out;

import java.util.List;

import com.umc.product.notice.domain.NoticeImage;

public interface SaveNoticeImagePort {
    NoticeImage saveImage(NoticeImage noticeImage);

    List<NoticeImage> saveAllImages(List<NoticeImage> noticeImages);

    void deleteImage(NoticeImage noticeImage);

    void deleteAllImagesByNoticeId(Long noticeId);

}
