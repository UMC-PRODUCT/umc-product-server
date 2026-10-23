package com.umc.product.notice.application.port.out;

import com.umc.product.notice.domain.NoticeTarget;

/**
 * NoticePermission 저장 Port
 */
public interface SaveNoticeTargetPort {

    /**
     * NoticePermission 저장
     *
     * @param noticeTarget 저장할 NoticePermission
     * @return 저장된 NoticePermission
     */
    NoticeTarget save(NoticeTarget noticeTarget);
}
