package com.umc.product.notice.application.port.out;

import com.umc.product.notice.domain.NoticeTarget;

/**
 * NoticePermission 관리 Port (삭제 등)
 */
public interface ManageNoticeTargetPort {

    /**
     * NoticePermission 삭제
     *
     * @param noticeTarget 삭제할 NoticePermission
     */
    void delete(NoticeTarget noticeTarget);

    /**
     * 공지사항 ID로 NoticePermission 삭제
     *
     * @param noticeId 공지사항 ID
     */
    void deleteByNoticeId(Long noticeId);
}
