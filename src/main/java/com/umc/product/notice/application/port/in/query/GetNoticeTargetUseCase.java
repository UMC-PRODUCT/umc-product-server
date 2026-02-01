package com.umc.product.notice.application.port.in.query;

import com.umc.product.notice.dto.NoticeTargetInfo;

public interface GetNoticeTargetUseCase {
    /**
     * 공지사항 ID로 해당 공지사항의 타겟을 조회합니다.
     */
    NoticeTargetInfo findByNoticeId(Long noticeId);
}
