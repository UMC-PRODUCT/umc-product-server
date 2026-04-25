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
     * classification.isStaffNotice()에 따라 챌린저/운영진 공지를 구분해 조회합니다.
     * 챌린저 공지: viewerInfo.memberParts() 기반 파트 필터
     * 운영진 공지: viewerInfo.roles() 기반 역할 overlap 필터 (roles는 readableRoles()로 확장된 값)
     */
    Page<Notice> findNoticesByClassification(NoticeClassification classification, NoticeViewerInfo viewerInfo,
                                             Pageable pageable);

    Page<Notice> findNoticesByKeyword(String keyword, NoticeClassification classification,
                                      NoticeViewerInfo viewerInfo, Pageable pageable);

    Page<Notice> findAllNotices(Pageable pageable);

}
