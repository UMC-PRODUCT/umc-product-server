package com.umc.product.inquiry.application.port.in.query;

import java.util.List;

/**
 * 문의 푸시를 받을 운영진을 계산한다. 운영진은 {@code UMC_PRODUCT} 시스템 역할을 직접 가진 회원이고, 작성자는 늘 뺀다.
 */
public interface ListInquiryStaffAlarmRecipientsUseCase {

    /**
     * 새 문의 알림을 켠 운영진.
     */
    List<Long> listNewInquiryRecipientMemberIds(Long authorMemberId);

    /**
     * 담당자가 있으면 담당자만 받는다. 없으면 그 문의에 메시지를 보낸 적 있는 운영진이고, 한 명도 없으면 새 문의 알림을 켠
     * 운영진이다. 담당자와 참여 운영진은 알림 설정과 상관없다.
     */
    List<Long> listAuthorMessageRecipientMemberIds(Long inquiryId);
}
