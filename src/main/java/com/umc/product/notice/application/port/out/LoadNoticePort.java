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
     * classification.tab()에 따라 공지를 구분해 조회합니다.
     * CHALLENGER: viewerInfo.memberParts() 기반 파트 필터
     * CENTRAL_STAFF: viewerInfo.roles() 중 중앙 역할 기반 overlap 필터
     * SCHOOL_STAFF: viewerInfo.roles() 중 교내 역할 기반 overlap 필터 + 소속 학교 범위
     * (roles는 readableRoles()로 확장된 값)
     */
    Page<Notice> findNoticesByClassification(NoticeClassification classification, NoticeViewerInfo viewerInfo,
                                             Pageable pageable);

    Page<Notice> findNoticesByKeyword(String keyword, NoticeClassification classification,
                                      NoticeViewerInfo viewerInfo, Pageable pageable);

    Page<Notice> findAllNotices(Pageable pageable);

}
