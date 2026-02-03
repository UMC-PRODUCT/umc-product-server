package com.umc.product.notice.application.port.out;

import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.enums.NoticeClassification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadNoticePort {
    Optional<Notice> findNoticeById(Long id);

    Page<Notice> findNoticesByClassification(NoticeClassification classification, Pageable pageable);

    Page<Notice> findNoticesByKeyword(String keyword, Pageable pageable);

    Page<Notice> findAllNotices(Pageable pageable);

    /**
     * 조회자(사용자) 기준으로 노출 가능한 공지 목록을 페이징 조회합니다.
     * 조회자의 gisu/chapter/part 조합과 schoolId를 이용해 타겟 매칭을 수행합니다.
     *
     * @param schoolId       조회자의 학교 ID
     * @param conditions     조회자의 gisu/chapter/part 조합
     * @param classification 선택 분류 필터 (null이면 전체)
     * @return 조회자에게 노출 가능한 공지 페이지
     */
    Page<Notice> findVisibleNotices(Long schoolId, List<NoticeTargetCondition> conditions,
                                    NoticeClassification classification, Pageable pageable);

}
