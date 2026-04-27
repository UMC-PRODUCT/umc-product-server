package com.umc.product.notice.application.port.out;

import com.umc.product.notice.application.port.in.query.dto.NoticeViewerInfo;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.dto.NoticeClassification;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadNoticePort {
    Optional<Notice> findNoticeById(Long id);

    /**
     * 챌린저 공지(gisu/chapter/school/part 조건)와 운영진 공지(viewerInfo.roles() overlap)를 함께 조회합니다.
     */
    Page<Notice> findNoticesByClassification(NoticeClassification classification, NoticeViewerInfo viewerInfo,
                                             Pageable pageable);

    Page<Notice> findNoticesByKeyword(String keyword, NoticeClassification classification,
                                      NoticeViewerInfo viewerInfo, Pageable pageable);

    Page<Notice> findAllNotices(Pageable pageable);

}
