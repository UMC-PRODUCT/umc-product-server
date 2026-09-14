package com.umc.product.notice.application.port.out;

import com.umc.product.notice.domain.NoticeImage;
import java.util.List;

public interface SaveNoticeImagePort {
    NoticeImage saveImage(NoticeImage noticeImage);

    List<NoticeImage> saveAllImages(List<NoticeImage> noticeImages);

    void deleteImage(NoticeImage noticeImage);

    void deleteAllImagesByNoticeId(Long noticeId);

}
